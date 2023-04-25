package ru.aasmc.taskmanager.tasks.controller

import org.springframework.web.bind.annotation.*
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.service.TaskService

@RestController
@RequestMapping("/tasks")
class TaskController(
    service: TaskService
): BaseController<Task>(service) {

}