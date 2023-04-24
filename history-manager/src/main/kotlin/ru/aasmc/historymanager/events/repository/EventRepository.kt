package ru.aasmc.historymanager.events.repository

import org.springframework.data.repository.CrudRepository
import ru.aasmc.historymanager.events.model.Event

interface EventRepository : CrudRepository<Event, Long> {
    fun findAllByTaskInfoTaskId(taskId: Long): List<Event>
}