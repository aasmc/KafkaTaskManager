package ru.aasmc.taskmanager.tasks.model

import ru.aasmc.taskmanager.events.model.TaskInfo
import java.util.*
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "tasks")
class Task : BaseTask() {


    override fun toString(): String {
        return "Task: [id=$id, name=$name, description=$description," +
                " taskStatus=$taskStatus, taskType=$taskType, " +
                "duration=$duration, startTime=$startTime]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        val o = other as? Task ?: return false
        return Objects.equals(name, o.name) &&
                Objects.equals(description, o.description) &&
                Objects.equals(startTime, o.startTime) &&
                Objects.equals(taskStatus, o.taskStatus) &&
                Objects.equals(taskType, o.taskType) &&
                Objects.equals(duration, o.duration)
    }

    override fun hashCode(): Int {
        return Objects.hashCode(
            arrayOf(
                super.hashCode(),
                name,
                description,
                startTime,
                taskStatus,
                taskType,
                duration
            )
        )
    }

    override fun toTaskInfo(): TaskInfo {
        return TaskInfo(
            taskId = id ?: 0,
            name = name,
            description = description,
            taskStatus = taskStatus,
            taskType = taskType,
            duration = duration,
            startTime = startTime
        )
    }
}