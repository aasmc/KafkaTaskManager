package ru.aasmc.taskmanager.tasks.model.mapper

import org.springframework.stereotype.Component
import ru.aasmc.taskmanager.tasks.dto.EpicDto
import ru.aasmc.taskmanager.tasks.model.Epic
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.model.TaskType
import ru.aasmc.taskmanager.tasks.repository.SubtaskRepository

@Component
class EpicMapper(
    private val subtaskRepository: SubtaskRepository
): IMapper<Epic, EpicDto> {
    override fun toEntity(dto: EpicDto): Epic {
        val children = if (dto.id == null) {
            mutableSetOf<SubTask>()
        } else {
            subtaskRepository.findAllByParentId(dto.id).toMutableSet()
        }
        return Epic().apply {
            id = dto.id
            name = dto.name
            description = dto.description
            taskStatus = dto.taskStatus
            taskType = TaskType.EPIC
            duration = dto.duration
            startTime = dto.startTime
            subtasks = children
        }
    }

    override fun toDto(entity: Epic): EpicDto {
        return EpicDto(
            id = entity.id,
            name = entity.name.orEmpty(),
            description = entity.description.orEmpty(),
            taskStatus = entity.taskStatus,
            duration = entity.duration ?: 0,
            startTime = entity.startTime,
            subtaskIds = entity.subtasks?.mapNotNull(SubTask::id) ?: emptyList()
        )
    }
}