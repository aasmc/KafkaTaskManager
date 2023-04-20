package ru.aasmc.taskmanager.events.service.kafka.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "topicprops")
class KafkaProperties @ConstructorBinding constructor(
        var partitions: Int,
        var replicas: Int,
        var validateRequestTopic: String,
        var validateResponseTopic: String,
        var replyTimeoutSeconds: Long,
        var bootstrapServers: String,
        var consumerAutoReset: String,
        var producerAckConfig: String,
        var debeziumOutboxTopicName: String
) {
}