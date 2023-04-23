package ru.aasmc.historymanager.events.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "kafkaprops")
class KafkaProperties @ConstructorBinding constructor(
    var groupId: String,
    var eventsTopicName: String,
    var bootstrapServers: String,
    var deadLetter: DeadLetter,
    var backoff: Backoff,
)

class DeadLetter(
    var retention: Duration,
    var suffix: String
)

class Backoff(
    var initialInterval: Duration,
    var maxInterval: Duration,
    var maxRetries: Int,
    var multiplier: Double
)