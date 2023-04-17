package ru.aasmc.taskmanager.events.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskmanager.util.DateProcessor
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "events")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0,
    @Column(name = "task_id")
    var taskId: Long,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
    @Column(name = "task_id", nullable = false, updatable = false)
    @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
    var eventDate: LocalDateTime,

    @Column(name = "event_type")
    @Enumerated(EnumType.STRING)
    var eventType: EventType
) {
    override fun toString(): String {
        return "Event: [id=$id, taskId=$taskId, eventDate=$eventDate, eventType=$eventType]"
    }
}
