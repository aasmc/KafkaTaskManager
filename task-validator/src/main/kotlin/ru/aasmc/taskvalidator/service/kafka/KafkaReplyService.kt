package ru.aasmc.taskvalidator.service.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import ru.aasmc.taskvalidator.dto.ValidationRequest
import ru.aasmc.taskvalidator.service.ValidationService

@Service
class KafkaReplyService(
        private val validationService: ValidationService
) {

    // KafkaListener echoes the correlation ID and determines the reply topic
    @KafkaListener(topics = ["\${topicprops.validateRequestTopic}"])
    @SendTo
    fun listen(record: ConsumerRecord<String, ValidationRequest>): Message<*> {
        val request = record.value()
        val response = validationService.validate(request)
        val correlationId = extractHeader(KafkaHeaders.CORRELATION_ID, record)
        val replyTopic = extractHeader(KafkaHeaders.REPLY_TOPIC, record)
        return MessageBuilder
                .withPayload(response)
                .setHeader(KafkaHeaders.MESSAGE_KEY, record.key())
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .setHeader(KafkaHeaders.REPLY_TOPIC, replyTopic)
                .build()
    }

    private fun extractHeader(key: String, record: ConsumerRecord<String, ValidationRequest>): ByteArray {
        return record.headers().lastHeader(key).value()
    }

}