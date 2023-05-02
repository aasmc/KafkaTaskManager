package ru.aasmc.taskmanager.tasks.model.mapper

import org.springframework.stereotype.Component
import ru.aasmc.taskmanager.tasks.dto.TaskDto
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.model.TaskType

@Component
class TaskMapper : IMapper<Task, TaskDto> {
    override fun toEntity(dto: TaskDto): Task {
        return Task().apply {
            id = dto.id
            name = dto.name
            description = dto.description
            taskStatus = dto.taskStatus
            taskType = TaskType.TASK
            duration = dto.duration
            startTime = dto.startTime
        }
    }

    override fun toDto(entity: Task): TaskDto {
        return TaskDto(
            id = entity.id,
            name = entity.name.orEmpty(),
            description = entity.description.orEmpty(),
            taskStatus = entity.taskStatus,
            duration = entity.duration ?: 0,
            startTime = entity.startTime!!
        )
    }
}