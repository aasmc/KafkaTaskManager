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