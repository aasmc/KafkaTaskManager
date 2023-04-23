package ru.aasmc.historymanager.events.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries
import ru.aasmc.historymanager.events.config.props.KafkaProperties

@Configuration
class KafkaConfig(
    private val props: KafkaProperties
) {

    @Bean
    fun deadLetterTopic(): NewTopic {
        return TopicBuilder
            .name("${props.eventsTopicName}${props.deadLetter.suffix}")
            .partitions(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "${props.deadLetter.retention.toMillis()}")
            .build()
    }

    @Bean
    fun defaultErrorHandler(
        template: KafkaOperations<*, *>
    ): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(template) { record, ex ->
            TopicPartition(record.topic() + props.deadLetter.suffix, 0)
        }
        val backoff = props.backoff
        val exponentialBackoff = ExponentialBackOffWithMaxRetries(backoff.maxRetries).apply {
            initialInterval = backoff.initialInterval.toMillis()
            multiplier = backoff.multiplier
            maxInterval = backoff.maxInterval.toMillis()
        }
        return DefaultErrorHandler(recoverer, exponentialBackoff).apply {
            addNotRetryableExceptions(JsonMappingException::class.java)
        }
    }

}