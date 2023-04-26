package ru.aasmc.taskmanager.tasks.model

import ru.aasmc.taskmanager.events.model.TaskInfo
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "subtasks")
class SubTask : BaseTask() {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var parent: Epic? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val o = other as? SubTask ?: return false
        return id != null && id == o.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "SubTask[id=$id, name=$name, description=$description, startTime=$startTime, " +
                "endTime=${endTime()}, parentId=${parent?.id}]"
    }

    override fun toTaskInfo(): TaskInfo {
        return TaskInfo(
            taskId = id ?: 0,
            name = name,
            description = description,
            taskStatus = taskStatus,
            taskType = taskType,
            duration = duration,
            startTime = startTime,
            parentId = parent?.id
        )
    }
}