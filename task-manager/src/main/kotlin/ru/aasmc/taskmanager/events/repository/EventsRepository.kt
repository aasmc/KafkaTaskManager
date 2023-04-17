package ru.aasmc.taskmanager.events.repository

import org.springframework.data.repository.CrudRepository
import ru.aasmc.taskmanager.events.model.Event

interface EventsRepository: CrudRepository<Event, Long> {
}