package ru.aasmc.taskmanager.tasks.service

import org.springframework.stereotype.Service
import ru.aasmc.taskmanager.events.service.kafka.ValidationService
import ru.aasmc.taskmanager.tasks.model.Epic
import ru.aasmc.taskmanager.tasks.repository.EpicRepository
import javax.transaction.Transactional

@Service
@Transactional
class EpicService(
    epicRepository: EpicRepository,
    eventService: EventService,
    validationService: ValidationService
): BaseService<Epic, EpicRepository>(epicRepository, validationService, eventService) {
}