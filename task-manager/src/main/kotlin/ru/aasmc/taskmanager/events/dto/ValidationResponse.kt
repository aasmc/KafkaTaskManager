package ru.aasmc.taskmanager.events.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ValidationResponse(
        @JsonProperty("task_id")
        val taskId: Long,
        val result: ValidationResult
)

enum class ValidationResult {
    SUCCESS,
    FAILURE
}