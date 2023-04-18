package ru.aasmc.taskmanager.tasks.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.model.EventType
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.repository.TaskRepository
import javax.transaction.Transactional

@Service
@Transactional
class TaskService(
    private val taskRepo: TaskRepository,
    private val eventService: EventService
) {

    companion object {
        val log = LoggerFactory.getLogger(TaskService::class.java)
    }

    fun getAllTasks(): List<Task> {
        val tasks = taskRepo.findAll().toList()
        tasks.forEach { t ->
            eventService.saveEvent(EventType.TASK_REQUESTED, t.id)
        }
        return tasks
    }

    fun deleteAllTasks() {
        taskRepo.findAll().forEach { t ->
            eventService.saveEvent(EventType.TASK_DELETED, t.id)
        }
        taskRepo.deleteAll()
    }

    fun getTaskById(id: Long): Task {
        val task = taskRepo.findById(id)
            .orElseThrow {
                NoSuchTaskException("No task with ID: $id is found!")
            }

        eventService.saveEvent(EventType.TASK_REQUESTED, task.id)
        return task
    }

    fun createTask(task: Task): Task {
        val created = taskRepo.save(task)
        val id = created.id
        val event = eventService.saveEvent(EventType.TASK_CREATED, id)
        log.info("Created Event in TaskService {}", event)
        return created
    }

    fun updateTask(task: Task): Task {
        val updated = taskRepo.save(task)

        val event = eventService.saveEvent(EventType.TASK_UPDATED, updated.id)
        return updated
    }

    fun deleteById(id: Long) {
        taskRepo.deleteById(id)
        eventService.saveEvent(EventType.TASK_DELETED, id)
    }
}