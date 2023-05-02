package ru.aasmc.taskmanager.tasks.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import ru.aasmc.taskmanager.tasks.model.TaskStatus
import ru.aasmc.taskmanager.util.DateProcessor
import java.time.LocalDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Positive

data class SubtaskDto(
    val id: Long? = null,
    @NotEmpty
    val name: String,
    @NotEmpty
    val description: String,
    @JsonProperty("task_status", required = true)
    val taskStatus: TaskStatus,
    @field:Positive
    val duration: Long = 0,
    @JsonProperty("start_time", required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    val startTime: LocalDateTime,
    @JsonProperty("parent_id")
    val parentId: Long
): BaseDto {
    override fun id(): Long? {
        return id
    }
}