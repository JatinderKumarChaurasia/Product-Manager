package com.jkc.microservices.api.event;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

public class EventModel<K, D> {

    private final EventModel.TYPE eventType;
    private final K key;
    private final D data;
    //    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//    @JsonSerialize(using = LocalDateTimeSerializer.class)
//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime eventCreatedAt;

    public EventModel() {
        this.eventType = null;
        this.key = null;
        this.data = null;
        this.eventCreatedAt = null;
    }

    public EventModel(TYPE eventType, K key, D data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        this.eventCreatedAt = now();
    }

    public TYPE getEventType() {
        return eventType;
    }

    public K getKey() {
        return key;
    }

    public D getData() {
        return data;
    }

    //
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
//    @JsonSerialize(using = LocalDateTimeSerializer.class)
//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }

    @Override
    public String toString() {
        return "EventModel{" +
                "eventType=" + eventType +
                ", key=" + key +
                ", data=" + data +
                ", eventCreatedAt=" + eventCreatedAt +
                '}';
    }

    public enum TYPE {
        CREATE,
        DELETE
    }
}
