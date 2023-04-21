package ru.aasmc.taskmanager.events.service.kafka.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListenerConfigurer
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import ru.aasmc.taskmanager.events.dto.ValidationRequest
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.service.kafka.props.KafkaProperties
import java.util.*

@Configuration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties
) : KafkaListenerConfigurer {

    @Value("\${spring.application.name}")
    private lateinit var applicationName: String

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

    @Bean
    fun replyConsumerFactory(): ConsumerFactory<String, ValidationResponse> {
        return DefaultKafkaConsumerFactory(consumerConfig())
    }

    @Bean
    fun replyingKafkaTemplate(
        repliesContainer: ConcurrentMessageListenerContainer<String, ValidationResponse>
    ): ReplyingKafkaTemplate<String, ValidationRequest, ValidationResponse> {
        val template = ReplyingKafkaTemplate(replyingProducerFactory(), repliesContainer)
        template.setSharedReplyTopic(true)
        return template
    }

    @Bean
    fun defaultErrorHandler(
        template: KafkaOperations<Any, Any>
    ): DefaultErrorHandler {
        // Publish to deadLetter topic any messages dropped after retries with backoff
        val recoverer = DeadLetterPublishingRecoverer(template) { record, ex ->
            // always send to first/only partition of DLT suffixed topic
            TopicPartition(record.topic() + kafkaProperties.deadLetter.suffix, 0)
        }
        // Spread out attempts over time, taking a little longer between each attempt
        // Set a max for retries below max.poll.interval.ms; default: 5m,
        // as otherwise we trigger a consumer rebalance
        val backoff = kafkaProperties.backoff
        val exponentialBackOff = ExponentialBackOffWithMaxRetries(backoff.maxRetries).apply {
            initialInterval = backoff.initialInterval.toMillis()
            multiplier = backoff.multiplier
            maxInterval = backoff.maxInterval.toMillis()
        }

        // do not try to recover from validation exceptions when validation of orders failed
        val errorHandler = DefaultErrorHandler(recoverer, exponentialBackOff).apply {
            addNotRetryableExceptions(javax.validation.ValidationException::class.java)
        }
        return errorHandler
    }

    @Bean
    fun replyingProducerFactory(): ProducerFactory<String, ValidationRequest> {
        val configs = mapOf<String, Any?>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to kafkaProperties.producerAckConfig
        )
        val keySerializer = StringSerializer()
        val valueSerializer = JsonSerializer<ValidationRequest>().noTypeInfo()
        return DefaultKafkaProducerFactory(configs, keySerializer, valueSerializer)
    }

    @Bean
    fun repliesContainer(
        containerFactory: ConcurrentKafkaListenerContainerFactory<String, ValidationResponse>,
        consumerFactory: ConsumerFactory<String, ValidationResponse>
    ): ConcurrentMessageListenerContainer<String, ValidationResponse> {
        containerFactory.consumerFactory = consumerFactory
        val container = containerFactory.createContainer(kafkaProperties.validateResponseTopic)
        // set random Group ID because:
        // When configuring with a single reply topic, each instance must use
        // a different group.id. In this case, all instances receive each reply,
        // but only the instance that sent the request finds the correlation ID.
        // For more details see: https://docs.spring.io/spring-kafka/reference/html/#replying-template
        containerFactory.containerProperties.setGroupId(UUID.randomUUID().toString())
        container.isAutoStartup = false
        return container
    }

    @Autowired
    private lateinit var validator: LocalValidatorFactoryBean

    override fun configureKafkaListeners(registrar: KafkaListenerEndpointRegistrar) {
        registrar.setValidator(this.validator)
    }

}