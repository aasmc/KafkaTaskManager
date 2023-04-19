package ru.aasmc.taskmanager.events.service.kafka.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import ru.aasmc.taskmanager.events.dto.ValidationRequest
import ru.aasmc.taskmanager.events.service.kafka.props.TopicProperties
import java.util.*

@Configuration
class KafkaConfig(
    private val topicProperties: TopicProperties
) {

    @Bean
    fun replyingKafkaTemplate(
        repliesContainer: ConcurrentMessageListenerContainer<String, String>
    ): ReplyingKafkaTemplate<String, ValidationRequest, String> {
        val template = ReplyingKafkaTemplate(replyingProducerFactory(), repliesContainer)
        template.setSharedReplyTopic(true)
        return template
    }

    @Bean
    fun replyingProducerFactory(): ProducerFactory<String, ValidationRequest> {
        val configs = mapOf<String, Any?>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:29092,localhost:39092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configs)
    }


    @Bean
    fun repliesContainer(
        containerFactory: ConcurrentKafkaListenerContainerFactory<String, String>
    ): ConcurrentMessageListenerContainer<String, String> {
        val container = containerFactory.createContainer(topicProperties.validateResponseTopic)
        // set random Group ID because:
        // When configuring with a single reply topic, each instance must use
        // a different group.id. In this case, all instances receive each reply,
        // but only the instance that sent the request finds the correlation ID.
        // For more details see: https://docs.spring.io/spring-kafka/reference/html/#replying-template
        containerFactory.containerProperties.setGroupId(UUID.randomUUID().toString())
        container.isAutoStartup = false
        return container
    }

}