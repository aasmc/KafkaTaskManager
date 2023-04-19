package ru.aasmc.taskmanager.events.service.kafka.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "topicprops")
class TopicProperties @ConstructorBinding constructor(
        var partitions: Int,
        var replicas: Int,
        var validateRequestTopic: String,
        var validateResponseTopic: String,
        var replyTimeoutSeconds: Long,
        var deleteTaskValidationTopic: String,
) {
}