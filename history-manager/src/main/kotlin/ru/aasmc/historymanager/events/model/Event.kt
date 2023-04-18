package ru.aasmc.historymanager.events.model

import javax.persistence.*

@Entity
@Table(name = "events")
class Event(
        @Id
        var id: Long,

        @Column(name = "task_id", nullable = false)
        var taskId: Long,

        @Column(name = "event_date", nullable = false)
        var eventDate: Long,

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
