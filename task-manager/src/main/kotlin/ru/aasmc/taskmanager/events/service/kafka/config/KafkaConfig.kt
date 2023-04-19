package ru.aasmc.taskmanager.events.service.kafka.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import ru.aasmc.taskmanager.events.dto.ValidationRequest
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.service.kafka.props.TopicProperties
import java.util.*

@Configuration
class KafkaConfig(
        private val topicProperties: TopicProperties
) {

    @Bean
    fun replyingKafkaTemplate(pf: ProducerFactory<String, ValidationRequest>,
                              repliesContainer: ConcurrentMessageListenerContainer<String, ValidationResponse>
    ): ReplyingKafkaTemplate<String, ValidationRequest, ValidationResponse> {
        return ReplyingKafkaTemplate(pf, repliesContainer)
    }


    @Bean
    fun repliesContainer(
            containerFactory: ConcurrentKafkaListenerContainerFactory<String, ValidationResponse>
    ): ConcurrentMessageListenerContainer<String, ValidationResponse> {
        val container = containerFactory.createContainer(topicProperties.validateResponseTopic)
        // set random Group ID because:
        // When configuring with a single reply topic, each instance must use
        // a different group.id. In this case, all instances receive each reply,
        // but only the instance that sent the request finds the correlation ID.
        // For more details see: https://docs.spring.io/spring-kafka/reference/html/#replying-template
        containerFactory.containerProperties.setGroupId(UUID.randomUUID().toString())
        val properties = containerFactory.containerProperties.kafkaConsumerProperties
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
        container.isAutoStartup = false
        return container
    }

}