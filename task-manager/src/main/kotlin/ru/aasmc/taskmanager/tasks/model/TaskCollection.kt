package ru.aasmc.taskmanager.tasks.model

data class TaskCollection<Entity: BaseTask>(
    val tasks: List<Entity>
)