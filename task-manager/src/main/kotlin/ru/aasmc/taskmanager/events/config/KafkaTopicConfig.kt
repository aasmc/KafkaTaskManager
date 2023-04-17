package ru.aasmc.taskmanager.events.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import ru.aasmc.taskmanager.events.config.prop.TopicProperties

@Configuration
class KafkaTopicConfig(
    private val props: TopicProperties
) {

    @Bean
    fun eventsTopic(): NewTopic {
        return TopicBuilder
            .name(props.topicName)
            .partitions(props.numPartitions)
            .replicas(props.numReplicas)
            .build()
    }

}