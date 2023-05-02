package ru.aasmc.taskmanager.tasks.model.mapper

import org.springframework.stereotype.Component
import ru.aasmc.taskmanager.tasks.dto.SubtaskDto
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.model.TaskType
import ru.aasmc.taskmanager.tasks.repository.EpicRepository

@Component
class SubtaskMapper(
    private val epicRepository: EpicRepository
): IMapper<SubTask, SubtaskDto> {
    override fun toEntity(dto: SubtaskDto): SubTask {
        val epic = epicRepository.findById(dto.parentId)
            .orElseThrow {
                NoSuchTaskException("Epic with id: ${dto.parentId} for SubTask with id: ${dto.id} is not found!")
            }
        return SubTask().apply {
            id = dto.id
            name = dto.name
            description = dto.description
            taskStatus = dto.taskStatus
            taskType = TaskType.SUBTASK
            duration = dto.duration
            startTime = dto.startTime
            parent = epic
        }
    }

    override fun toDto(entity: SubTask): SubtaskDto {
        return SubtaskDto(
            id = entity.id,
            name = entity.name.orEmpty(),
            description = entity.description.orEmpty(),
            taskStatus = entity.taskStatus,
            duration = entity.duration ?: 0,
            startTime = entity.startTime!!,
            parentId = entity.parent?.id!!
        )
    }
}