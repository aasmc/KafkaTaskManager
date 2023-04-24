package ru.aasmc.historymanager.events.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.historymanager.util.DateProcessor
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Embeddable
class TaskInfo(
    @Column(name = "task_id", nullable = false)
    var taskId: Long,
    @Column(nullable = true)
    var name: String? = null,
    @Column(nullable = true)
    var description: String? = null,
    @Column(name = "task_status", nullable = true)
    @Enumerated(EnumType.STRING)
    var taskStatus: TaskStatus? = null,
    @Column(name = "task_type", nullable = true)
    @Enumerated(EnumType.STRING)
    var taskType: TaskType? = null,
    @Column(nullable = true)
    var duration: Long? = null,
    @Column(name = "start_time", nullable = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
    var startTime: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskInfo

        if (taskId != other.taskId) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (taskStatus != other.taskStatus) return false
        if (taskType != other.taskType) return false
        if (duration != other.duration) return false
        return startTime == other.startTime
    }

    override fun hashCode(): Int {
        var result = taskId.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (taskStatus?.hashCode() ?: 0)
        result = 31 * result + (taskType?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (startTime?.hashCode() ?: 0)
        return result
    }
}