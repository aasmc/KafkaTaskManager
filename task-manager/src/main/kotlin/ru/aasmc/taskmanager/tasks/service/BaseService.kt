package ru.aasmc.taskmanager.tasks.service

import org.springframework.data.repository.CrudRepository
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.dto.ValidationResult
import ru.aasmc.taskmanager.events.model.CrudEventType
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.dto.Controllable
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.exception.TaskIntersectionException
import ru.aasmc.taskmanager.tasks.model.BaseTask

abstract class BaseService<DTO: Controllable, Entity: BaseTask, Repository: CrudRepository<Entity, Long>>(
    protected val repo: Repository,
    protected val validationService: ValidationService,
    protected val eventService: EventService
) {

    open fun getAllTasks(): List<Entity> {
        val tasks = repo.findAll().toList()
        tasks.forEach { t ->
            eventService.saveEvent(CrudEventType.TASK_REQUESTED, t.toTaskInfo())
        }
        return tasks
    }

    open fun deleteAllTasks() {
        repo.findAll().forEach { t ->
            deleteValidationInfo(t)
            eventService.saveEvent(CrudEventType.TASK_DELETED, t.toTaskInfo())
        }
        repo.deleteAll()
    }

    open fun getTaskById(id: Long): Entity {
        val task = repo.findById(id)
            .orElseThrow { NoSuchTaskException("No task with ID: $id is found!") }

        eventService.saveEvent(CrudEventType.TASK_REQUESTED, task.toTaskInfo())
        return task
    }

    open fun createTask(task: Entity): Entity {
        validateTask(task)
        val created = repo.save(task)
        eventService.saveEvent(CrudEventType.TASK_CREATED, task.toTaskInfo())
        return created
    }

    protected open fun validateTask(task: Entity) {
        val response = validationService.requestValidation(task)
        validateInternal(response, task)
    }

    protected fun validateInternal(response: ValidationResponse?, task: Entity) {
        response?.let { resp ->
            if (validationFailed(resp)) {
                throw TaskIntersectionException("Task with id ${task.id} intersects with other tasks.\n" +
                        "Only one task at a time is allowed!")
            }
        } ?: throw RuntimeException("Unknown Server Error while sending validation" +
                " request to Kafka and receiving validation response " +
                "for task with id ${task.id}")
    }


    protected open fun validationFailed(response: ValidationResponse): Boolean {
        return response.result == ValidationResult.FAILURE
    }

    open fun updateTask(task: Entity): Entity {
        deleteValidationInfo(task)
        validateTask(task)
        val updated = repo.save(task)
        eventService.saveEvent(CrudEventType.TASK_UPDATED, updated.toTaskInfo())
        return updated
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