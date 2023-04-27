package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.dto.SubtaskDto
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.model.Epic
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.repository.SubtaskRepository
import javax.transaction.Transactional

@Service
@Transactional
class SubTaskService(
    subtaskRepository: SubtaskRepository,
    eventService: EventService,
    validationService: ValidationService
): BaseService<SubtaskDto, SubTask, SubtaskRepository>(subtaskRepository, validationService, eventService) {

    fun getEpicOfSubTask(id: Long) : Epic {
        val sub = repo.findById(id)
                .orElseThrow {
                    NoSuchTaskException("SubTask with ID: $id is not found!")
                }
        return sub.parent
                ?: throw IllegalStateException("Subtask was saved to DB without parent! Impossible condition!")
    }

}