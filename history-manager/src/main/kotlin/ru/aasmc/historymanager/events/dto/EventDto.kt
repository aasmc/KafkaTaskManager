package ru.aasmc.historymanager.events.dto

import ru.aasmc.historymanager.events.model.Event
import ru.aasmc.historymanager.events.model.EventType

data class EventDto(
        val id: Long,
        val task_id: Long,
        val event_date: Long,
        val event_type: String,
        val version: Int
)

fun EventDto.toDomain(): Event {
    return Event(
            id = id,
            taskId = task_id,
            eventDate = event_date,
            eventType = EventType.valueOf(event_type),
            version = version
    )
}
