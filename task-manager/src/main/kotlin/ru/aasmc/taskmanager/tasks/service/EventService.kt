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

    fun saveEvent(type: CrudEventType, taskInfo: TaskInfo): CrudEvent {
        return eventRepo.save(CrudEvent(
                taskInfo = taskInfo,
                eventDate = LocalDateTime.now(),
                eventType = type
        ))
    }

}