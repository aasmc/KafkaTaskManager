package ru.aasmc.taskmanager.tasks.dto

sealed interface Controllable {
    fun id(): Long?
}