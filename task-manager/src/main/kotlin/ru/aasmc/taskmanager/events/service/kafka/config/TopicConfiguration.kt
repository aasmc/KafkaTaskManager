package ru.aasmc.taskmanager.events.service.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import ru.aasmc.taskmanager.events.service.kafka.props.KafkaProperties

@Configuration
class TopicConfiguration(
        private val kafkaProperties: KafkaProperties
) {

    @Bean
    fun validateRequestTopic(): NewTopic {
        return TopicBuilder
                .name(kafkaProperties.validateRequestTopic)
                .partitions(kafkaProperties.partitions)
                .replicas(kafkaProperties.replicas)
                .build()
    }

    @Bean
    fun debeziumOutboxTopic(): NewTopic {
        return TopicBuilder
                .name(kafkaProperties.debeziumOutboxTopicName)
                .partitions(kafkaProperties.partitions)
                .replicas(kafkaProperties.replicas)
                .build()
    }

    /**
     * Dead Letter Topic which is used for sending ConsumerRecords that have not
     * been processed due to some error either non-recoverable (i.e. no retries
     * were made for that error), or after all retry attempts have been exhausted.
     */
    @Bean
    fun deadLetterTopic(): NewTopic {
        val retentionMillis = kafkaProperties.deadLetter.retention.toMillis()
        return TopicBuilder
            .name(kafkaProperties.validateResponseTopic)
            // Use only 1 partition for infrequently used Dead Letter Topic
            .partitions(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "$retentionMillis")
            .build()
    }

}