package ru.aasmc.taskmanager.tasks.dto

sealed interface BaseDto {
    fun id(): Long?
}