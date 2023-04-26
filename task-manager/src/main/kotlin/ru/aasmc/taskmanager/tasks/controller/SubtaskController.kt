package ru.aasmc.taskmanager.tasks.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.aasmc.taskmanager.tasks.model.Epic
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.service.SubTaskService

private val log = LoggerFactory.getLogger(SubtaskController::class.java)

@RestController
@RequestMapping("/subtasks")
class SubtaskController(
        private val subTaskService: SubTaskService
): BaseController<SubTask>(subTaskService) {

    @GetMapping("/{id}/epic")
    fun getEpicOfSubTask(@PathVariable("id") id: Long): Epic {
        log.debug("Fetching Epic of Subtask with id: $id")
        return subTaskService.getEpicOfSubTask(id)
    }

}