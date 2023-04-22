package ru.aasmc.taskvalidator.service.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import ru.aasmc.taskvalidator.dto.ValidationRequest
import ru.aasmc.taskvalidator.service.ValidationService

@Service
class KafkaReplyService(
        private val validationService: ValidationService
) {

    // KafkaListener echoes the correlation ID and determines the reply topic
    @KafkaListener(topics = ["\${kafkaprops.validateRequestTopic}"])
    @SendTo
    fun listen(@Payload @Validated request: ValidationRequest): Message<*> {
        val response = validationService.validate(request)
        return MessageBuilder
                .withPayload(response)
                .build()
    }

}