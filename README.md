# Kafka Task Manager

Educational project created with the goal of learning Kafka Connect with Debezium.

For now, user can create/update/delete/get Task from TaskManager microservice.
At the same time TaskManager microservice saves CRUD events to a separate table used by Debezium as
a source of messages for Kafka. When a row changes in the events table, a message is sent to the Kafka topic 
events.public.crud_events. 

HistoryManager microservice tracks all events by listening to the Kafka topic and saves the events
to its database. 

## Validation
TaskManager microservice and Validation microservice use Kafka to exchange info about validation.
TaskManager configures ReplyingKafkaTemplate to send validation requests to a Kafka topic validate-request, and receive
response from Validation microservice, which consumes messages from the topic, validates the request and
sends back request to another Kafka topic validate-response (configured in ReplyingKafkaTemplate). 

To allow for multiple instances of TaskManager in the role of the Kafka Consumer, we need to specify unique group.id for
each instance. Instances will therefore consume all messages from all partitions of the reply topic, but will react
only to those messages which they sent. This is achieved by KafkaHeaders.CORRELATION_ID. 

To enable multiple TaskManager consumers we need to specify unique group.id Kafka property. And there's a problem
with deserializing Json response. Below is the map of consumer properties in TaskManager with more detailed explanation:

```kotlin
    @Bean
    fun consumerConfig(): Map<String, Any?> {
        return hashMapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                // If Consumer group.id is not set to a random value, we have a problem:
                // when multiple instances of TaskManager (aka producer of validation requests, and
                // consumer of validation responses) are up, each of them needs to consume only its own
                // messages - this is achieved by KafkaHeaders.CORRELATION_ID, but if each instance is in
                // the same Consumer Group, then it will consume messages only from a specific partition,
                // so we may lose some reply messages, which causes a TimeoutException.
                // Spring Docs say: When configuring with a single reply topic, each instance must use
                // a different group.id. In this case, all instances receive each reply,
                // but only the instance that sent the request finds the correlation ID.
                // For more details see: https://docs.spring.io/spring-kafka/reference/html/#replying-template
                // Although it is said that ContainerProperties.setGroupId() overrides any {@code group.id} property
                // provided by the consumer factory configuration, this doesn't seem to work. So I had to
                // write custom ConsumerFactory bean and set consumer group.id to a random value here.
                ConsumerConfig.GROUP_ID_CONFIG to "$applicationName-${UUID.randomUUID()}",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to kafkaProperties.consumerAutoReset,
                // Don't use typeId from Kafka, because in response/reply pattern it causes errors
                // while deserializing received ConsumerRecord. To achieve proper behaviour, we need to add
                // default type for deserialization, and tell Jackson to ignore type headers.
                JsonDeserializer.VALUE_DEFAULT_TYPE to ValidationResponse::class.java,
                JsonDeserializer.USE_TYPE_INFO_HEADERS to false
        )
    }
```

Kafka consumer in Validation microservice must also be properly configured to handle deserialization. Below is the 
application.yml config:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:29092, localhost:39092
    producer:
      acks: all
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      bootstrap-servers: localhost:29092, localhost:39092
    consumer:
      auto-offset-reset: latest
      bootstrap-servers: localhost:29092, localhost:39092
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
        spring.json.value.default.type: "ru.aasmc.taskvalidator.dto.ValidationRequest"
      group-id: ${topicprops.groupId}
```


## TBD
1. Let user retrieve history of task changes.
2. Validate task intersection (only one task at a time is allowed) DONE for Task
3. Add different types of tasks: Epic, SubTask
4. Create a gateway with all endpoints



## Preparing the environment:
To prepare the environment, go to the **docker** folder and execute:
```bash
docker-compose up -d
```

It brings up the following containers:
1. Kafka (2 brokers)
2. ZooKeeper
3. postgres (3 instances: one for TaskManager microservice, one for HistoryManager microservice, one for Kadeck)
4. cmak (UI for managing Kafka cluster)
5. Debezium Kafka connector
6. Kadeck (another UI to track what's happening in Kafka)

After the containers are up and running, connect Debezium with Postgres by executing the following 
script from the **connector** directory:
```bash
curl -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
http://localhost:8083/connectors/ \
-d @debezium-postgres.json
```
After that you are ready to start microservices. 

To test the application, go to tasks folder, and execute the following commands:
```bash
curl -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
http://localhost:8082/tasks/ \
-d @one.json

curl -i -X POST -H "Accept:application/json" \
-H "Content-Type:application/json" \
http://localhost:8082/tasks/ \
-d @one.json
```

To check that the validation works, execute them several times. 

## Known issues:

1. Debezium fails to create partitions and replicas for topic events.public.crud_events, although configs for the topic
is specified in the json debezium configuration. Solution - manually create topic in TaskManager microservice
(since it's where the Debezium connector is reading its data). 