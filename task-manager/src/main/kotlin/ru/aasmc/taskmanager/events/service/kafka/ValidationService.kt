package ru.aasmc.taskmanager.events.service.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.dto.ValidationRequest
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.service.kafka.props.KafkaProperties
import ru.aasmc.taskmanager.tasks.client.ValidatorClient
import ru.aasmc.taskmanager.tasks.dto.DeleteTaskValidationRequest
import ru.aasmc.taskmanager.tasks.model.BaseTask
import ru.aasmc.taskmanager.tasks.model.Task
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class ValidationService(
    private val replyTemplate: ReplyingKafkaTemplate<String, ValidationRequest, ValidationResponse>,
    private val kafkaProperties: KafkaProperties,
    private val validatorClient: ValidatorClient
) {

    companion object {
        val log = LoggerFactory.getLogger(ValidationService::class.java)
    }

    fun requestValidation(
        task: BaseTask
    ): ValidationResponse? {
        log.info("Start sending validation request to Kafka.")
        val validationRequest = ValidationRequest(
            taskStartTime = task.startTime,
            taskEndTime = task.endTime(),
            taskId = task.id
        )

        val producerRecord = ProducerRecord<String, ValidationRequest>(
            kafkaProperties.validateRequestTopic,
            validationRequest
        )

        val sendResult = replyTemplate.sendAndReceive(
            producerRecord,
            Duration.ofSeconds(kafkaProperties.replyTimeoutSeconds)
        )
        log.debug("Send result from Kafka {}", sendResult)
        val response = sendResult.get(kafkaProperties.replyTimeoutSeconds, TimeUnit.SECONDS)
        return response?.value()
    }

    fun deleteValidationInfo(task: BaseTask) {
        validatorClient.deleteValidationInfo(
            DeleteTaskValidationRequest(task.startTime, task.endTime())
        )
    }

}