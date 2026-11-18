package com.andesstay.reservations.dto;

import java.time.LocalDateTime;

public class ReservationStreamEvent {
    private String eventId;
    private String eventType;
    private Long reservationId;
    private String guestName;
    private String roomType;
    private String status;
    private String performedBy;
    private String timestamp;
    private String details;

    public ReservationStreamEvent() {}

    public ReservationStreamEvent(String eventId, String eventType, Long reservationId, String guestName,
                                  String roomType, String status, String performedBy, String details) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.status = status;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now().toString();
        this.details = details;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}