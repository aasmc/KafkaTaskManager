package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.model.Event
import ru.aasmc.taskmanager.events.model.EventType
import ru.aasmc.taskmanager.events.repository.EventsRepository
import java.time.LocalDateTime

@Service
//@Transactional(value = Transactional.TxType.REQUIRES_NEW)
class EventService(
        private val eventRepo: EventsRepository
) {

    fun saveEvent(type: EventType, taskId: Long): Event {
        val saved = eventRepo.save(Event(
                taskId = taskId,
                eventDate = LocalDateTime.now(),
                eventType = type
        ))
        return saved
    }

}