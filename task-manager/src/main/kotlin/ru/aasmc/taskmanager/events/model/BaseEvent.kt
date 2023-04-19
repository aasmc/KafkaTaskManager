package ru.aasmc.taskmanager.events.model

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import ru.aasmc.taskmanager.util.DateProcessor
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@MappedSuperclass
abstract class BaseEvent(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateProcessor.DATE_FORMAT)
        @Column(name = "event_date", nullable = false)
        @DateTimeFormat(pattern = DateProcessor.DATE_FORMAT)
        var eventDate: LocalDateTime,
        @Version
        var version: Int = 0,
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val o = (other) as? BaseEvent ?: return false
        if (!Objects.equals(id, o.id)) return false
        return true
    }

    override fun hashCode(): Int {
        return id?.toInt() ?: 0
    }

    override fun toString(): String {
        return "BaseEvent:[id=$id, $eventDate=${DateProcessor.toString(eventDate)}, " +
                "version=$version]"
    }

}