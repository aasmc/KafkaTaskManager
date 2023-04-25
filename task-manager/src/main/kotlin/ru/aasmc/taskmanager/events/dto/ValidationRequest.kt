package ru.aasmc.taskmanager.events.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskmanager.util.DateProcessor
import java.time.LocalDateTime

data class ValidationRequest(
        @JsonProperty("task_start_time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        val taskStartTime: LocalDateTime?,
        @JsonProperty("task_end_time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        val taskEndTime: LocalDateTime?,
        @JsonProperty("task_id")
        val taskId: Long?
)