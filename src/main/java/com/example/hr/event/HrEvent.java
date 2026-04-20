package com.example.hr.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Base event class cho HR system.
 */
public abstract class HrEvent extends ApplicationEvent {

    private final String actorUsername;
    private final String eventType;
    private final LocalDateTime timestamp;

    protected HrEvent(Object source, String actorUsername, String eventType) {
        super(source);
        this.actorUsername = actorUsername;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public String getActorUsername() { return actorUsername; }
    public String getEventType() { return eventType; }
    public LocalDateTime getEventTime() { return timestamp; }
}
