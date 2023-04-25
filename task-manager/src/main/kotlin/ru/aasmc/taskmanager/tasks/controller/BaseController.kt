package ru.aasmc.taskmanager.tasks.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import ru.aasmc.taskmanager.tasks.model.BaseTask
import ru.aasmc.taskmanager.tasks.model.TaskCollection
import ru.aasmc.taskmanager.tasks.service.BaseService

abstract class BaseController<Entity: BaseTask>(
    private val service: BaseService<Entity, *>
) {

    companion object {
        private val log = LoggerFactory.getLogger(BaseController::class.java)
    }

    @GetMapping
    fun getAllTasks(): TaskCollection<Entity> {
        log.debug("Fetching all tasks!")
        val allTasks = service.getAllTasks()
        return TaskCollection(allTasks)
    }

    @GetMapping("/{id}")
    fun getTaskById(@PathVariable("id") id: Long): Entity {
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
    fun createTask(@RequestBody task: Entity): Entity {
        log.debug("Creating task $task")
        return service.createTask(task)
    }

    @PutMapping
    fun updateTask(@RequestBody task: Entity): Entity {
        log.debug("Updating task $task")
        return service.updateTask(task)
    }
}