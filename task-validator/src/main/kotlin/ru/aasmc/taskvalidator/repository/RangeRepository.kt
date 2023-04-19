package ru.aasmc.taskvalidator.repository

import org.springframework.data.repository.CrudRepository
import ru.aasmc.taskvalidator.model.Range
import java.time.LocalDateTime

interface RangeRepository: CrudRepository<Range, Long> {
    fun findAllByStartBeforeAndEndAfter(start: LocalDateTime, endLocalDateTime: LocalDateTime): List<Range>

    fun deleteRangeByEndAndStart(start: LocalDateTime, endLocalDateTime: LocalDateTime)
}