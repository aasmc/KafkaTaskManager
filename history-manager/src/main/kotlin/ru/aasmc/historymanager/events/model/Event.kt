package ru.aasmc.historymanager.events.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.historymanager.util.DateProcessor
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "events")
class Event(
    @Id
    var id: Long? = null,

    @Column(name = "task_info", nullable = false)
    @Embedded
    var taskInfo: TaskInfo,

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
        return "Event: [id=$id, taskId=${taskInfo.taskId}, eventDate=$eventDate, eventType=$eventType]"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        val o = other as? Event ?: return false
        return id != null && Objects.equals(id, other.id)
                && taskInfo == other.taskInfo
    }

    override fun hashCode(): Int {
        return javaClass.hashCode() + taskInfo.hashCode()
    }
}
