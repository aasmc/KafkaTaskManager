package ru.aasmc.historymanager.events.dto

import com.fasterxml.jackson.annotation.JsonProperty
import ru.aasmc.historymanager.events.model.Event
import ru.aasmc.historymanager.events.model.EventType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class EventDto(
        val id: Long,
        @JsonProperty("task_id")
        val taskId: Long,
        @JsonProperty("event_date")
        val eventDate: Long,
        @JsonProperty("event_type")
        val eventType: EventType,
        val version: Int
)

fun EventDto.toDomain(): Event {
    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(eventDate), ZoneId.systemDefault())
    return Event(
            id = id,
            taskId = taskId,
            eventDate = date,
            eventType = eventType,
            version = version
    )
}
