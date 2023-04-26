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
                .compact()
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
                .partitions(kafkaProperties.partitions)
                .replicas(kafkaProperties.replicas)
                .build()
    }

}