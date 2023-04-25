package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.model.SubTask
import ru.aasmc.taskmanager.tasks.repository.SubtaskRepository
import javax.transaction.Transactional

@Service
@Transactional
class SubTaskService(
    subtaskRepository: SubtaskRepository,
    eventService: EventService,
    validationService: ValidationService
): BaseService<SubTask, SubtaskRepository>(subtaskRepository, validationService, eventService) {
}