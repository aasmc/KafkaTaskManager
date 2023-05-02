package ru.aasmc.taskmanager.tasks.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import ru.aasmc.taskmanager.tasks.model.TaskStatus
import ru.aasmc.taskmanager.util.DateProcessor
import java.time.LocalDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.PositiveOrZero

data class EpicDto(
    val id: Long? = null,
    @NotEmpty
    val name: String,
    @NotEmpty
    val description: String,
    @JsonProperty("task_status", required = true)
    val taskStatus: TaskStatus,
    @field:PositiveOrZero
    val duration: Long = 0,
    @JsonProperty("start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    val startTime: LocalDateTime? = null,
    @JsonProperty("subtask_ids")
    val subtaskIds: List<Long>
): BaseDto {
    override fun id(): Long? {
        return id
    }
}
