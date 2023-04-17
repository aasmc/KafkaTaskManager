package ru.aasmc.taskmanager.tasks.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.model.TaskCollection
import ru.aasmc.taskmanager.tasks.service.TaskService

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val service: TaskService
) {
    companion object {
        val log = LoggerFactory.getLogger(TaskController::class.java)
    }

    @GetMapping
    fun getAllTasks(): TaskCollection {
        log.debug("Fetching all tasks!")
        val allTasks = service.getAllTasks()
        return TaskCollection(allTasks)
    }

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable("id") id: Long): Task {
        log.debug("Searching for task with ID: $id.")
        return service.getTaskById(id)
    }

    @DeleteMapping("/{id}")
    fun deleteTaskById(@PathVariable("id") id: Long) {
        log.debug("Deleting task by ID: $id.")
        service.deleteById(id)
    }

    @DeleteMapping
    fun deleteAllTasks() {
        log.debug("Deleting all tasks.")
        service.deleteAllTasks()
    }

    @PostMapping
    fun createTask(@RequestBody task: Task): Task {
        log.debug("Creating task $task")
        return service.createTask(task)
    }

    @PutMapping
    fun updateTask(@RequestBody task: Task): Task {
        log.debug("Updating task $task")
        return service.updateTask(task)
    }
}