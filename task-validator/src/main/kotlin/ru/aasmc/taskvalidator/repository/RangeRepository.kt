package ru.aasmc.taskvalidator.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import ru.aasmc.taskvalidator.model.Range
import java.time.LocalDateTime

interface RangeRepository: CrudRepository<Range, Long> {
    @Query("select r from Range r where r.start <= :end and r.end >= :start")
    fun findAllOverlappings(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime): List<Range>

    @Modifying
    @Query("delete from Range r where r.start = :start and r.end = :end")
    fun deleteRangeByStartAndEnd(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime)
}