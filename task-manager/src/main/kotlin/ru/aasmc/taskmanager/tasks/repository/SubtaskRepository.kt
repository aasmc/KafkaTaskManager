package ru.aasmc.taskmanager.tasks.repository

import org.springframework.data.repository.CrudRepository
import ru.aasmc.taskmanager.tasks.model.SubTask

interface SubtaskRepository: CrudRepository<SubTask, Long> {
}