package ru.aasmc.taskmanager.events.dto

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Positive

data class ValidationResponse(
        @JsonProperty("task_id")
        @Positive
        val taskId: Long,
        val result: ValidationResult
)

enum class ValidationResult {
    SUCCESS,
    FAILURE
}