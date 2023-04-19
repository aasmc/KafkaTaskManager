package ru.aasmc.taskmanager.events.repository

import org.springframework.data.repository.CrudRepository
import ru.aasmc.taskmanager.events.model.CrudEvent

interface CrudEventsRepository: CrudRepository<CrudEvent, Long> {
}