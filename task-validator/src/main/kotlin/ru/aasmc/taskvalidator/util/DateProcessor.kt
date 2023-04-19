package ru.aasmc.taskvalidator.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateProcessor {

    const val DATE_FORMAT = "yyyy-MM-dd HH:mm"

    private val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)

    fun toDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date, formatter)
    }

    fun toString(date: LocalDateTime): String {
        return date.format(formatter)
    }
}