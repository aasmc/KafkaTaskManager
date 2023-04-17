package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.model.Event
import ru.aasmc.taskmanager.events.model.EventType
import ru.aasmc.taskmanager.events.repository.EventsRepository
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.repository.TaskRepository
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
@Transactional
class TaskService(
    private val taskRepo: TaskRepository,
    private val eventRepo: EventsRepository
) {

    fun getAllTasks(): List<Task> {
        val tasks = taskRepo.findAll().toList()
        tasks.forEach { t ->
            eventRepo.save(
                Event(
                    taskId = t.id,
                    eventDate = LocalDateTime.now(),
                    eventType = EventType.TASK_REQUESTED
                )
            )
        }
        return tasks
    }

    fun deleteAllTasks() {
        taskRepo.findAll().forEach { t ->
            eventRepo.save(
                Event(
                    taskId = t.id,
                    eventDate = LocalDateTime.now(),
                    eventType = EventType.TASK_DELETED
                )
            )
        }
        taskRepo.deleteAll()
    }

    fun getTaskById(id: Long): Task {
        val task = taskRepo.findById(id)
            .orElseThrow {
                NoSuchTaskException("No task with ID: $id is found!")
            }

        eventRepo.save(
            Event(
                taskId = task.id,
                eventDate = LocalDateTime.now(),
                eventType = EventType.TASK_REQUESTED
            )
        )

        return task
    }

    fun createTask(task: Task): Task {
        val created = taskRepo.save(task)

        eventRepo.save(
            Event(
                taskId = created.id,
                eventDate = LocalDateTime.now(),
                eventType = EventType.TASK_CREATED
            )
        )

        return created
    }

    fun updateTask(task: Task): Task {
        val updated = taskRepo.save(task)

        eventRepo.save(
            Event(
                taskId = updated.id,
                eventDate = LocalDateTime.now(),
                eventType = EventType.TASK_UPDATED
            )
        )

        return updated
    }

    fun deleteById(id: Long) {
        taskRepo.deleteById(id)
        eventRepo.save(
            Event(
                taskId = id,
                eventDate = LocalDateTime.now(),
                eventType = EventType.TASK_DELETED
            )
        )
    }
}