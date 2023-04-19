package ru.aasmc.taskmanager.events.service.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import ru.aasmc.taskmanager.events.service.kafka.props.TopicProperties

@Configuration
class TopicConfiguration(
        private val topicProperties: TopicProperties
) {

    @Bean
    fun validateRequestTopic(): NewTopic {
        return TopicBuilder
                .name(topicProperties.validateRequestTopic)
                .partitions(topicProperties.partitions)
                .replicas(topicProperties.replicas)
                .build()
    }

}