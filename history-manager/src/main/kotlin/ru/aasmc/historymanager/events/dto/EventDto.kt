package ru.aasmc.historymanager.events.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import ru.aasmc.historymanager.events.model.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@JsonInclude(JsonInclude.Include.NON_NULL)
data class EventDto(
    val id: Long,
    @JsonProperty("task_id")
    val taskId: Long,
    val name: String?,
    val description: String?,
    @JsonProperty("task_status")
    val taskStatus: TaskStatus?,
    @JsonProperty("task_type")
    val taskType: TaskType?,
    val duration: Long?,
    @JsonProperty("start_time")
    val startTime: Long?,
    @JsonProperty("event_date")
    val eventDate: Long,
    @JsonProperty("event_type")
    val eventType: EventType,
    val version: Int
)

fun EventDto.toDomain(): Event {
    val eDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(eventDate), ZoneId.systemDefault())

    val sTime = startTime?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
    }

    val taskInfo = TaskInfo(
        taskId,
        name,
        description,
        taskStatus,
        taskType,
        duration,
        sTime
    )

    return Event(
        id = id,
        taskInfo = taskInfo,
        eventDate = eDate,
        eventType = eventType,
        version = version
    )
}
