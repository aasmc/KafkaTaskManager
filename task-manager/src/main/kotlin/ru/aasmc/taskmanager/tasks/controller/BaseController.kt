package ru.aasmc.taskmanager.tasks.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import ru.aasmc.taskmanager.tasks.dto.BaseDto
import ru.aasmc.taskmanager.tasks.model.BaseTask
import ru.aasmc.taskmanager.tasks.model.TaskCollection
import ru.aasmc.taskmanager.tasks.service.BaseService
import javax.validation.Valid

abstract class BaseController<DTO: BaseDto, Entity: BaseTask>(
    protected val service: BaseService<DTO, Entity, *>
) {

    companion object {
        private val log = LoggerFactory.getLogger(BaseController::class.java)
    }

    @GetMapping
    open fun getAllTasks(): TaskCollection<DTO> {
        log.debug("Fetching all tasks!")
        val allTasks = service.getAllTasks()
        return TaskCollection(allTasks)
    }

    @GetMapping("/{id}")
    open fun getTaskById(@PathVariable("id") id: Long): DTO {
        log.debug("Searching for task with ID: $id.")
        return service.getTaskById(id)
    }

    @DeleteMapping("/{id}")
    open fun deleteTaskById(@PathVariable("id") id: Long) {
        log.debug("Deleting task by ID: $id.")
        service.deleteById(id)
    }

    @DeleteMapping
    open fun deleteAllTasks() {
        log.debug("Deleting all tasks.")
        service.deleteAllTasks()
    }

    @PostMapping
    open fun createTask(@Valid @RequestBody task: DTO): DTO {
        log.debug("Creating task $task")
        return service.createTask(task)
    }

    @PutMapping
    open fun updateTask(@Valid @RequestBody task: DTO): DTO {
        log.debug("Updating task $task")
        return service.updateTask(task)
    }
}