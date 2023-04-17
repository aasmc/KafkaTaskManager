package ru.aasmc.taskmanager.events.config.prop

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "events")
data class TopicProperties(
    val topicName: String,
    val numPartitions: Int,
    val numReplicas: Int
)
