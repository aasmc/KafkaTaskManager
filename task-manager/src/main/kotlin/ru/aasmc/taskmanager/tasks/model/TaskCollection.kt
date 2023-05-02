package ru.aasmc.taskmanager.tasks.model

import ru.aasmc.taskmanager.tasks.dto.BaseDto

data class TaskCollection<DTO: BaseDto>(
    val tasks: List<DTO>
)