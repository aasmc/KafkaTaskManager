package ru.aasmc.taskmanager.events.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "crud_events")
class CrudEvent (
        eventDate: LocalDateTime,
        @Column(name = "event_type")
        @Enumerated(EnumType.STRING)
        var eventType: CrudEventType,
        @Embedded
        var taskInfo: TaskInfo,
): BaseEvent(eventDate = eventDate) {
    override fun toString(): String {
        return "Event: [id=$id, taskId=${taskInfo.taskId}, eventDate=$eventDate, eventType=$eventType]"
    }
}
