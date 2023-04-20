package ru.aasmc.taskmanager.events.service.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.dto.ValidationRequest
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.service.kafka.props.KafkaProperties
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Service
class ValidationKafkaService(
        private val replyTemplate: ReplyingKafkaTemplate<String, ValidationRequest, String>,
        private val kafkaProperties: KafkaProperties,
        private val objectMapper: ObjectMapper
) {

    companion object {
        val log = LoggerFactory.getLogger(ValidationKafkaService::class.java)
    }

    fun requestValidation(
            startTime: LocalDateTime,
            endTime: LocalDateTime,
            taskId: Long
    ): ValidationResponse? {
        log.info("Start sending validation request to Kafka.")
        val validationRequest = ValidationRequest(
                taskStartTime = startTime,
                taskEndTime = endTime,
                taskId = taskId
        )

        val producerRecord = ProducerRecord<String, ValidationRequest>(
                kafkaProperties.validateRequestTopic,
                validationRequest
        )

        val sendResult = replyTemplate.sendAndReceive(producerRecord,
                Duration.ofSeconds(kafkaProperties.replyTimeoutSeconds))
        log.debug("Send result from Kafka {}", sendResult)
        val response = sendResult.get(kafkaProperties.replyTimeoutSeconds, TimeUnit.SECONDS)
        return response?.value()?.let { resp ->
            objectMapper.readValue(resp, ValidationResponse::class.java)
        }
    }

}