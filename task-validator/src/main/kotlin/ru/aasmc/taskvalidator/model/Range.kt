package ru.aasmc.taskvalidator.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskvalidator.util.DateProcessor
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "ranges")
class Range(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @Column(name = "start_time", nullable = false)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        var start: LocalDateTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @Column(name = "end_time", nullable = false)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        var end: LocalDateTime
)