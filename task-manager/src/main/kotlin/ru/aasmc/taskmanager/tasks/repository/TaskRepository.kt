package ru.aasmc.taskmanager.tasks.repository

import org.springframework.data.repository.CrudRepository
import ru.aasmc.taskmanager.tasks.model.Task

interface TaskRepository : CrudRepository<Task, Long> {

}