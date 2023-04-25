package ru.aasmc.taskmanager.tasks.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.util.DateProcessor
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@MappedSuperclass
abstract class BaseTask{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    var id: Long? = null

    @Column(name = "task_name", nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var description: String? = null

    @Column(name = "task_status", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    var taskStatus: TaskStatus = TaskStatus.NEW

    @Column(name = "task_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    var taskType: TaskType = TaskType.TASK

    @Column(nullable = false)
    var duration: Long? = null

    @Column(name = "start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
    var startTime: LocalDateTime? = null

    @Version
    var version: Int = 0

    fun endTime(): LocalDateTime? {
        val duration = Duration.ofMinutes(duration ?: 0)
        return startTime?.plus(duration)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseTask
        if (!Objects.equals(id, other.id)) return false
        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "AbstractEntity[id=$id, name=$name, description=$description, " +
                "taskStatus=$taskStatus, taskType=$taskType, duration=${duration}, " +
                "startTime=${startTime}, endTime=${endTime()}, version=$version]"
    }

    abstract fun toTaskInfo(): TaskInfo
}

