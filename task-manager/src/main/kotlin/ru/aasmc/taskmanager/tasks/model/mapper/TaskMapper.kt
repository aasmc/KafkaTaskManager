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
}