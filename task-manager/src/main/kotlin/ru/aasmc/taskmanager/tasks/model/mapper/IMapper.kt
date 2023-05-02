package ru.aasmc.taskmanager.tasks.model.mapper

import ru.aasmc.taskmanager.tasks.dto.BaseDto

interface IMapper<Entity, Dto: BaseDto> {
    fun toEntity(dto: Dto): Entity

    fun toDto(entity: Entity): Dto
}