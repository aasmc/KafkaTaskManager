package ru.aasmc.taskmanager.tasks.model.mapper

interface IMapper<Entity, Dto> {
    fun toEntity(dto: Dto): Entity
}