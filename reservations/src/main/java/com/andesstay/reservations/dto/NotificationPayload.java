package com.andesstay.reservations.dto;

import java.io.Serializable;

public class NotificationPayload implements Serializable {
    private Long reservationId;
    private String guestEmail;
    private String guestName;
    private String unitId;
    private String status;
    private String message;

    public NotificationPayload() {}

    public NotificationPayload(Long reservationId, String guestEmail, String guestName, String unitId, String status, String message) {
        this.reservationId = reservationId;
        this.guestEmail = guestEmail;
        this.guestName = guestName;
        this.unitId = unitId;
        this.status = status;
        this.message = message;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}