package ru.aasmc.taskvalidator.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "topicprops")
class TopicProperties @ConstructorBinding constructor(
        var validateRequestTopic: String
) {
}