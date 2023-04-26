package ru.aasmc.taskmanager.tasks.model

import ru.aasmc.taskmanager.events.model.TaskInfo
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "epics")
class Epic : BaseTask() {

    override var taskStatus: TaskStatus
        get() = getTaskStatusInternal()
        set(value) {}
    override fun endTime(): LocalDateTime? {
        return subtasks.mapNotNull(SubTask::endTime)
            .maxOfOrNull { it }
    }

    override var startTime: LocalDateTime?
        get() = getStartTimeInternal()
        set(value) {}

    @OneToMany(
        mappedBy = "parent",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    var subtasks: MutableList<SubTask> = arrayListOf()

    fun addSubTask(subTask: SubTask) {
        subtasks.add(subTask)
        subTask.parent = this
    }

    fun removeSubtask(subTask: SubTask) {
        subtasks.remove(subTask)
        subTask.parent = null
    }

    private fun getStartTimeInternal(): LocalDateTime? {
        return subtasks.mapNotNull(SubTask::startTime)
            .minOrNull()
    }

    private fun getTaskStatusInternal(): TaskStatus {
        var isNew = true
        var isDone = true
        subtasks.forEach { s ->
            val status = s.taskStatus
            isNew = isNew && status == TaskStatus.NEW
            isDone = isDone && status == TaskStatus.DONE
        }
        if (isNew) {
            return TaskStatus.NEW
        }
        if (isDone) {
            return TaskStatus.DONE
        }
        return TaskStatus.IN_PROGRESS
    }

    override fun toString(): String {
        return "Epic[id=$id, name=$name, description=$description]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        val o = other as? Epic ?: return false
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
            startTime = startTime,
            subtaskIds = subtasks.joinToString(separator = ",")
        )
    }
}
