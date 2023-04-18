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
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,

        @Embedded
        var taskInfo: TaskInfo,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @Column(name = "event_date", nullable = false)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        var eventDate: LocalDateTime,

        @Column(name = "event_type")
        @Enumerated(EnumType.STRING)
        var eventType: EventType,

        @Version
        var version: Int = 0
) {
    override fun toString(): String {
        return "Event: [id=$id, taskId=${taskInfo.taskId}, eventDate=$eventDate, eventType=$eventType]"
    }
}
