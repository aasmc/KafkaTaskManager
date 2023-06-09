package ru.aasmc.taskmanager.events.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskmanager.tasks.model.TaskStatus
import ru.aasmc.taskmanager.tasks.model.TaskType
import ru.aasmc.taskmanager.util.DateProcessor
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
    var startTime: LocalDateTime? = null,
    @Column(name = "subtask_ids", nullable = true)
    var subtaskIds: String? = null,
    @Column(name = "parent_id")
    var parentId: Long? = null
)
