package ru.aasmc.taskmanager.tasks.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.aasmc.taskmanager.tasks.model.Epic
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.model.TaskCollection
import ru.aasmc.taskmanager.tasks.service.EpicService

private val log = LoggerFactory.getLogger(EpicController::class.java)
@RestController
@RequestMapping("/epics")
class EpicController(
        private val epicService: EpicService
): BaseController<Epic>(epicService) {

    @GetMapping("/{id}/subtasks")
    fun getSubtasksOfEpic(@PathVariable("id") id: Long): TaskCollection<SubTask> {
        log.debug("Fetching all subtasks of Epic with id: $id")
        val allSubs = epicService.getAllSubtasksOfEpic(id)
        return TaskCollection(allSubs)
    }

}