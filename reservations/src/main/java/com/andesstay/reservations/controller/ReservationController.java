package com.andesstay.reservations.controller;

import com.andesstay.reservations.dto.NotificationPayload;
import com.andesstay.reservations.model.Reservation;
import com.andesstay.reservations.producer.ReservationMessageProducer;
import com.andesstay.reservations.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final ReservationMessageProducer messageProducer;

    public ReservationController(ReservationRepository reservationRepository,
                                 ReservationMessageProducer messageProducer) {
        this.reservationRepository = reservationRepository;
        this.messageProducer = messageProducer;
    }

    // Captura fechas con formato inválido (ej. 99-99-9999) y devuelve un 400 claro
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleInvalidFormat(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                Map.of("message", "Formato de datos o fecha inválido. Use fechas reales en formato YYYY-MM-DD.")
        );
    }

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {
        if (reservation.getGuestName() == null || reservation.getGuestName().trim().length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("message", "El nombre del huésped es obligatorio (mínimo 3 caracteres)."));
        }
        if (reservation.getCheckInDate() == null || reservation.getCheckOutDate() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Las fechas de Check-In y Check-Out son obligatorias."));
        }

        LocalDate today = LocalDate.now();
        LocalDate maxAllowedDate = today.plusYears(3);

        if (reservation.getCheckInDate().isBefore(today.minusDays(1)) || reservation.getCheckInDate().isAfter(maxAllowedDate)
                || reservation.getCheckOutDate().isAfter(maxAllowedDate)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Fecha fuera de rango permitido. Ingrese una fecha válida entre hoy y los próximos 3 años."));
        }

        if (!reservation.getCheckOutDate().isAfter(reservation.getCheckInDate())) {
            return ResponseEntity.badRequest().body(Map.of("message", "La fecha de Check-Out debe ser posterior a la fecha de Check-In."));
        }

        if (reservation.getStatus() == null || reservation.getStatus().isEmpty()) {
            reservation.setStatus("CREADA");
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        NotificationPayload payload = new NotificationPayload(
                savedReservation.getId(),
                "contacto@huesped.cl",
                savedReservation.getGuestName(),
                savedReservation.getRoomType() != null ? savedReservation.getRoomType() : "UNIDAD-1",
                savedReservation.getStatus(),
                "Su reserva #" + savedReservation.getId() + " ha sido recibida con exito en estado CREADA."
        );
        messageProducer.sendEmailNotification(payload);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedReservation);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El campo status es requerido"));
        }

        return reservationRepository.findById(id).map(reservation -> {
            if (("CHECKIN_PENDIENTE".equalsIgnoreCase(newStatus) || "EN_ESTADIA".equalsIgnoreCase(newStatus))
                    && !"CONFIRMADA".equalsIgnoreCase(reservation.getStatus())
                    && !"CHECKIN_PENDIENTE".equalsIgnoreCase(reservation.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Regla de negocio: No se puede hacer check-in sin estar CONFIRMADA."));
            }

            reservation.setStatus(newStatus.toUpperCase());
            Reservation updated = reservationRepository.save(reservation);

            NotificationPayload payload = new NotificationPayload(
                    updated.getId(),
                    "contacto@huesped.cl",
                    updated.getGuestName(),
                    updated.getRoomType() != null ? updated.getRoomType() : "UNIDAD-1",
                    updated.getStatus(),
                    "Actualizacion de reserva #" + updated.getId() + ": " + updated.getStatus()
            );

            if ("CONFIRMADA".equalsIgnoreCase(newStatus)) {
                messageProducer.sendEmailNotification(payload);
                messageProducer.sendHousekeepingTicket(payload);
                messageProducer.sendVoucherGeneration(payload);
            } else {
                messageProducer.sendEmailNotification(payload);
            }

            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}