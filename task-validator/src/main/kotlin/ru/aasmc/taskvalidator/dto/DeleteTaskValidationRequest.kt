package ru.aasmc.taskvalidator.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskvalidator.util.DateProcessor
import java.time.LocalDateTime

data class DeleteTaskValidationRequest(
        @JsonProperty("start_time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        val startTime: LocalDateTime,
        @JsonProperty("end_time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        val endTime: LocalDateTime
)

