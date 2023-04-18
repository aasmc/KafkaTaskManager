package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.model.Event
import ru.aasmc.taskmanager.events.model.EventType
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.events.repository.EventsRepository
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.model.toTaskInfo
import java.time.LocalDateTime

@Service
class EventService(
        private val eventRepo: EventsRepository
) {

    fun saveEvent(type: EventType, taskInfo: TaskInfo): Event {
        return eventRepo.save(Event(
                taskInfo = taskInfo,
                eventDate = LocalDateTime.now(),
                eventType = type
        ))
    }

}