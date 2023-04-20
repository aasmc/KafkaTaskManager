package ru.aasmc.taskmanager.events.service.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import ru.aasmc.taskmanager.events.service.kafka.props.KafkaProperties

@Configuration
class TopicConfig(
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

}