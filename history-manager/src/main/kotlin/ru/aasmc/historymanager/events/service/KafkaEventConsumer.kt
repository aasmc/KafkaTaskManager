package ru.aasmc.historymanager.events.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import ru.aasmc.historymanager.events.dto.EventDto
import ru.aasmc.historymanager.events.dto.toDomain
import ru.aasmc.historymanager.events.repository.EventRepository

@Service
class KafkaEventConsumer(
        private val objectMapper: ObjectMapper,
        private val eventRepository: EventRepository
) {
    companion object {
        val log = LoggerFactory.getLogger(KafkaEventConsumer::class.java)
    }

    @KafkaListener(topics = ["\${topicName}"], groupId = "\${spring.application.name}", autoStartup = "true")
    fun consume(record: ConsumerRecord<String, String>) {
        log.info("Consuming message from Kafka topic {}", record.topic())
        val recordNode = objectMapper.readTree(record.value())
        val payloadNode = recordNode.get("payload")
        val afterNode = payloadNode.get("after")
        val event = objectMapper.treeToValue(afterNode, EventDto::class.java)
        log.info("Received event from Kafka {}", event)
        val savedEvent = eventRepository.save(event.toDomain())
        log.info("Saved event to repository: {}", savedEvent)
    }
}