package com.andesstay.reservations.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class MessageEnvelope<T> implements Serializable {

    private String eventId;
    private String type;
    private String timestamp;
    private String traceId;
    private String correlationId;
    private T payload;

    public MessageEnvelope() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = Instant.now().toString();
        this.traceId = UUID.randomUUID().toString();
        this.correlationId = UUID.randomUUID().toString();
    }

    public MessageEnvelope(String type, T payload) {
        this();
        this.type = type;
        this.payload = payload;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}