package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.dto.EpicDto
import ru.aasmc.taskmanager.tasks.dto.SubtaskDto
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.model.Epic
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.model.mapper.EpicMapper
import ru.aasmc.taskmanager.tasks.model.mapper.SubtaskMapper
import ru.aasmc.taskmanager.tasks.repository.SubtaskRepository
import javax.transaction.Transactional

@Service
@Transactional
class SubTaskService(
    subtaskRepository: SubtaskRepository,
    eventService: EventService,
    validationService: ValidationService,
    subtaskMapper: SubtaskMapper,
    private val epicMapper: EpicMapper
): BaseService<SubtaskDto, SubTask, SubtaskRepository>(
    subtaskRepository,
    validationService,
    eventService,
    subtaskMapper
) {

    fun getEpicOfSubTask(id: Long) : EpicDto {
        val sub = repo.findById(id)
                .orElseThrow {
                    NoSuchTaskException("SubTask with ID: $id is not found!")
                }
        sub.parent
        return sub.parent?.let { epicMapper.toDto(it) }
                ?: throw IllegalStateException("Subtask was saved to DB without parent! Impossible condition!")
    }

}