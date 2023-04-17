package ru.aasmc.taskmanager.events.service

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.config.prop.TopicProperties
import ru.aasmc.taskmanager.events.model.Event

@Service
class EventsProducer(
    private val kafkaTemplate: KafkaTemplate<String, Event>,
    private val props: TopicProperties
) {

    companion object {
        val log = LoggerFactory.getLogger(EventsProducer::class.java)
    }

    @Scheduled(cron = "\${sendInterval}")
    fun sendEvent(event: Event) {
        // TODO read events from DB
        log.debug("Sending event $event to Kafka.")
        kafkaTemplate.send(props.topicName, event.eventType.name, event)
    }

}