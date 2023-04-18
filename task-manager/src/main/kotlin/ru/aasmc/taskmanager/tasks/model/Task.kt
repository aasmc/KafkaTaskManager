package ru.aasmc.taskmanager.tasks.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.util.DateProcessor
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "tasks")
class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    var id: Long,

    @Column(name = "task_name", nullable = false)
    var name: String,

    @Column(nullable = false)
    var description: String,

    @Column(name = "task_status", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    var taskStatus: TaskStatus,

    @Column(name = "task_type", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    var taskType: TaskType,

    @Column(nullable = false)
    var duration: Long,

    @Column(name = "start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
    var startTime: LocalDateTime,

    @Version
    var version: Int
) {
    override fun toString(): String {
        return "Task: [id=$id, name=$name, description=$description," +
                " taskStatus=$taskStatus, taskType=$taskType, " +
                "duration=$duration, startTime=$startTime]"
    }
}

fun Task.toTaskInfo(): TaskInfo {
    return TaskInfo(
        taskId = id,
        name = name,
        description = description,
        taskStatus = taskStatus,
        taskType = taskType,
        duration = duration,
        startTime = startTime
    )
}