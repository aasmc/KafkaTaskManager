package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.model.CrudEventType
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.dto.EpicDto
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.model.Epic
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.repository.EpicRepository
import javax.transaction.Transactional

@Service
@Transactional
class EpicService(
        epicRepository: EpicRepository,
        eventService: EventService,
        validationService: ValidationService
) : BaseService<EpicDto, Epic, EpicRepository>(epicRepository, validationService, eventService) {

    override fun deleteAllTasks() {
        repo.findAll().forEach { epic ->
            eventService.saveEvent(CrudEventType.TASK_DELETED, epic.toTaskInfo())
            epic.subtasks?.forEach { sub ->
                validationService.deleteValidationInfo(sub)
                eventService.saveEvent(CrudEventType.TASK_DELETED, sub.toTaskInfo())
            }
        }
        repo.deleteAll()
    }

    override fun validateTask(task: Epic) {
        task.subtasks?.map(validationService::requestValidation)
                ?.forEach { response ->
                    validateInternal(response, task)
                }
    }

    override fun deleteValidationInfo(t: Epic) {
        t.subtasks?.forEach(validationService::deleteValidationInfo)
    }

    override fun deleteById(id: Long) {
        var epic: Epic? = null
        repo.findById(id).ifPresent { e ->
            epic = e
            deleteValidationInfo(e)
            e.subtasks?.forEach { sub ->
                eventService.saveEvent(CrudEventType.TASK_DELETED, sub.toTaskInfo())
            }
        }
        epic?.let {
            repo.deleteById(id)
            eventService.saveEvent(CrudEventType.TASK_DELETED, it.toTaskInfo())
        }
    }

    fun getAllSubtasksOfEpic(id: Long): List<SubTask> {
        val epic = repo.findById(id)
                .orElseThrow {
                    NoSuchTaskException("Epic with ID: $id is not found!")
                }
        val subsList = epic.subtasks?.toList() ?: emptyList()
        subsList.forEach { sub ->
            eventService.saveEvent(CrudEventType.TASK_REQUESTED, sub.toTaskInfo())
        }
        return subsList
    }
}