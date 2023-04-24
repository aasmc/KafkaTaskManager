package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.model.CrudEvent
import ru.aasmc.taskmanager.events.model.CrudEventType
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.events.repository.CrudEventsRepository
import java.time.LocalDateTime

@Service
class EventService(
        private val eventRepo: CrudEventsRepository
) {

    fun saveEvent(type: CrudEventType, taskInfo: TaskInfo) {
        val saved = eventRepo.save(CrudEvent(
                taskInfo = taskInfo,
                eventDate = LocalDateTime.now(),
                eventType = type
        ))
        // immediately remove the event from DB so as not to occupy space:
        // the event will be forwarded to Kafka topic by Debezium. Delete
        // events will be ignored because of the setting in connector:
        // "tombstones.on.delete" : "false"
        eventRepo.deleteById(saved.id!!)
    }

}