package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.dto.TaskDto
import ru.aasmc.taskmanager.tasks.model.Task
import ru.aasmc.taskmanager.tasks.model.mapper.TaskMapper
import ru.aasmc.taskmanager.tasks.repository.TaskRepository
import javax.transaction.Transactional

@Service
@Transactional
class TaskService(
    taskRepo: TaskRepository,
    eventService: EventService,
    validationService: ValidationService,
    taskMapper: TaskMapper
): BaseService<TaskDto, Task, TaskRepository>(taskRepo, validationService, eventService, taskMapper) {

}