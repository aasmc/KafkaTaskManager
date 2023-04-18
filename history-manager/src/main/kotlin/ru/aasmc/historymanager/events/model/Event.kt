package ru.aasmc.historymanager.events.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.historymanager.util.DateProcessor
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "events")
class Event(
        @Id
        var id: Long,

        @Column(name = "task_id", nullable = false)
        var taskId: Long,

        @Column(name = "event_date", nullable = false)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        var eventDate: LocalDateTime,

        @Column(name = "event_type")
        @Enumerated(EnumType.STRING)
        var eventType: EventType,

        @Version
        var version: Int = 0
) {
    override fun toString(): String {
        return "Event: [id=$id, taskId=$taskId, eventDate=$eventDate, eventType=$eventType]"
    }
}
