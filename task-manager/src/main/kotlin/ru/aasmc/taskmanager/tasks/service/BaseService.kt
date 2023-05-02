package ru.aasmc.taskmanager.tasks.service

import org.springframework.data.repository.CrudRepository
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.dto.ValidationResult
import ru.aasmc.taskmanager.events.model.CrudEventType
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.dto.BaseDto
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.exception.TaskIntersectionException
import ru.aasmc.taskmanager.tasks.model.BaseTask
import ru.aasmc.taskmanager.tasks.model.mapper.IMapper

abstract class BaseService<DTO: BaseDto, Entity: BaseTask, Repository: CrudRepository<Entity, Long>>(
    protected val repo: Repository,
    protected val validationService: ValidationService,
    protected val eventService: EventService,
    protected val mapper: IMapper<Entity, DTO>
) {

    open fun getAllTasks(): List<DTO> {
        val tasks = repo.findAll().toList()
        tasks.forEach { t ->
            eventService.saveEvent(CrudEventType.TASK_REQUESTED, t.toTaskInfo())
        }
        return tasks.map(mapper::toDto)
    }

    open fun deleteAllTasks() {
        repo.findAll().forEach { t ->
            deleteValidationInfo(t)
            eventService.saveEvent(CrudEventType.TASK_DELETED, t.toTaskInfo())
        }
        repo.deleteAll()
    }

    open fun getTaskById(id: Long): DTO {
        val task = repo.findById(id)
            .orElseThrow { NoSuchTaskException("No task with ID: $id is found!") }

        eventService.saveEvent(CrudEventType.TASK_REQUESTED, task.toTaskInfo())
        return mapper.toDto(task)
    }

    open fun createTask(task: DTO): DTO {
        validateTask(task)
        val created = repo.save(mapper.toEntity(task))
        eventService.saveEvent(CrudEventType.TASK_CREATED, created.toTaskInfo())
        return mapper.toDto(created)
    }

    protected open fun validateTask(task: DTO) {
        val response = validationService.requestValidation(mapper.toEntity(task))
        validateInternal(response, task)
    }

    protected fun validateInternal(response: ValidationResponse?, task: DTO) {
        response?.let { resp ->
            if (validationFailed(resp)) {
                throw TaskIntersectionException("Task with id ${task.id()} intersects with other tasks.\n" +
                        "Only one task at a time is allowed!")
            }
        } ?: throw RuntimeException("Unknown Server Error while sending validation" +
                " request to Kafka and receiving validation response " +
                "for task with id ${task.id()}")
    }


    protected open fun validationFailed(response: ValidationResponse): Boolean {
        return response.result == ValidationResult.FAILURE
    }

    open fun updateTask(task: DTO): DTO {
        deleteValidationInfo(mapper.toEntity(task))
        validateTask(task)
        val updated = repo.save(mapper.toEntity(task))
        eventService.saveEvent(CrudEventType.TASK_UPDATED, updated.toTaskInfo())
        return mapper.toDto(updated)
    }

    open fun deleteById(id: Long) {
        repo.findById(id).ifPresent { t ->
            deleteValidationInfo(t)
        }
        repo.deleteById(id)
        eventService.saveEvent(CrudEventType.TASK_DELETED, TaskInfo(taskId = id))
    }

    protected open fun deleteValidationInfo(t: Entity) {
        validationService.deleteValidationInfo(t)
    }

}