package ru.aasmc.taskvalidator.config

import jakarta.validation.ValidationException
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.TopicConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListenerConfigurer
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import ru.aasmc.taskvalidator.config.props.KafkaProperties

@Configuration
@EnableKafka
class KafkaConfig(
    private val props: KafkaProperties
) : KafkaListenerConfigurer {

    @Autowired
    private lateinit var validatorFactory: LocalValidatorFactoryBean
    override fun configureKafkaListeners(registrar: KafkaListenerEndpointRegistrar) {
        registrar.setValidator(validatorFactory)
    }

    @Bean
    fun deadLetterTopic(): NewTopic {
        return TopicBuilder
            .name("${props.validateRequestTopic}${props.deadLetter.suffix}")
            .partitions(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "${props.deadLetter.retention.toMillis()}")
            .build()
    }

    @Bean
    fun defaultErrorHandler(
        template: KafkaOperations<*, *>
    ): DefaultErrorHandler {
        // Publish to deadLetter topic any messages dropped after retries with backoff
        val recoverer = DeadLetterPublishingRecoverer(template) { record, ex ->
            // always send to first/only partition of DLT suffixed topic
            TopicPartition(record.topic() + props.deadLetter.suffix, 0)
        }
        // Spread out attempts over time, taking a little longer between each attempt
        // Set a max for retries below max.poll.interval.ms; default: 5m,
        // as otherwise we trigger a consumer rebalance
        val backoff = props.backoff
        val exponentialBackOff = ExponentialBackOffWithMaxRetries(backoff.maxRetries).apply {
            initialInterval = backoff.initialInterval.toMillis()
            multiplier = backoff.multiplier
            maxInterval = backoff.maxInterval.toMillis()
        }

        // do not try to recover from validation exceptions when validation of orders failed
        val errorHandler = DefaultErrorHandler(recoverer, exponentialBackOff).apply {
            addNotRetryableExceptions(ValidationException::class.java)
        }
        return errorHandler
    }
}