package ru.aasmc.taskvalidator.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "kafkaprops")
class KafkaProperties @ConstructorBinding constructor(
    var partitions: Int,
    var replicas: Int,
    var validateRequestTopic: String,
    var deadLetter: DeadLetter,
    var backoff: Backoff,
    var groupId: String,
    var bootstrapServers: String,
    var producerAck: String,
    var consumerAutoOffsetReset: String
) {
}

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