package ru.aasmc.taskmanager.tasks.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.dto.ValidationResponse
import ru.aasmc.taskmanager.events.dto.ValidationResult
import ru.aasmc.taskmanager.events.model.CrudEventType
import ru.aasmc.taskmanager.events.model.TaskInfo
import ru.aasmc.taskmanager.events.service.kafka.ValidationKafkaService
import ru.aasmc.taskmanager.tasks.client.ValidatorClient
import ru.aasmc.taskmanager.tasks.dto.DeleteTaskValidationRequest
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.exception.TaskIntersectionException
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.model.toTaskInfo
import ru.aasmc.taskmanager.tasks.repository.TaskRepository
import javax.transaction.Transactional

@Service
@Transactional
class TaskService(
        private val taskRepo: TaskRepository,
        private val eventService: EventService,
        private val validationService: ValidationKafkaService,
        private val validatorClient: ValidatorClient
) {

    companion object {
        val log = LoggerFactory.getLogger(TaskService::class.java)
    }

    fun getAllTasks(): List<Task> {
        val tasks = taskRepo.findAll().toList()
        tasks.forEach { t ->
            eventService.saveEvent(CrudEventType.TASK_REQUESTED, t.toTaskInfo())
        }
        return tasks
    }

    fun deleteAllTasks() {
        taskRepo.findAll().forEach { t ->
            deleteValidationInfo(t)
            eventService.saveEvent(CrudEventType.TASK_DELETED, t.toTaskInfo())
        }
        taskRepo.deleteAll()
    }

    private fun deleteValidationInfo(task: Task) {
        validatorClient.deleteValidationInfo(
                DeleteTaskValidationRequest(task.startTime, task.getEndTime())
        )
    }

    fun getTaskById(id: Long): Task {
        val task = taskRepo.findById(id)
                .orElseThrow {
                    NoSuchTaskException("No task with ID: $id is found!")
                }

        eventService.saveEvent(CrudEventType.TASK_REQUESTED, task.toTaskInfo())
        return task
    }

    fun createTask(task: Task): Task {
        validateTask(task)
        val created = taskRepo.save(task)
        val event = eventService.saveEvent(CrudEventType.TASK_CREATED, task.toTaskInfo())
        log.info("Created Event in TaskService {}", event)
        return created
    }

    // TODO add retries
    private fun validateTask(task: Task) {
        validationService.requestValidation(task.startTime,
                task.getEndTime(),
                task.id,
                onSuccess = { record ->
                    record?.value()?.let { response ->
                        if (validationFailed(response)) {
                            throw TaskIntersectionException("Task with id ${task.id} intersects with other tasks.\n" +
                                    "Only one task at a time is allowed!")
                        }
                    } ?: throw RuntimeException("Unknown Server Error while sending validation" +
                            " request to Kafka and receiving validation response " +
                            "for task with id ${task.id}")
                },
                onFailure = { ex ->
                    throw RuntimeException(ex.cause)
                }
        )
    }

    private fun validationFailed(response: ValidationResponse): Boolean {
        return response.result == ValidationResult.FAILURE
    }

    fun updateTask(task: Task): Task {
        deleteValidationInfo(task)
        validateTask(task)
        val updated = taskRepo.save(task)
        eventService.saveEvent(CrudEventType.TASK_UPDATED, updated.toTaskInfo())
        return updated
    }

    fun deleteById(id: Long) {
        taskRepo.findById(id).ifPresent { t ->
            deleteValidationInfo(t)
        }
        taskRepo.deleteById(id)
        eventService.saveEvent(CrudEventType.TASK_DELETED, TaskInfo(taskId = id))
    }
}