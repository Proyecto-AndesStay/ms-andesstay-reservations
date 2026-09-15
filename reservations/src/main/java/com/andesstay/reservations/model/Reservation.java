package com.andesstay.reservations.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = true)
    private String guestEmail;

    @Column(nullable = true)
    private Long propertyId;

    @Column(nullable = true)
    private LocalDate checkInDate;

    @Column(nullable = true)
    private LocalDate checkOutDate;

    @Column(nullable = true)
    private BigDecimal totalPrice;

    @Column(nullable = true)
    private String status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Reservation() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null || this.status.isEmpty()) {
            this.status = "CREADA";
        }
        if (this.guestEmail == null) {
            this.guestEmail = "huesped@andesstay.com";
        }
        if (this.propertyId == null) {
            this.propertyId = 101L;
        }
        if (this.totalPrice == null) {
            this.totalPrice = new BigDecimal("120000.00");
        }
        if (this.checkInDate == null) {
            this.checkInDate = LocalDate.now();
        }
        if (this.checkOutDate == null) {
            this.checkOutDate = LocalDate.now().plusDays(2);
        }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
