package com.andesstay.reservations.controller;

import com.andesstay.reservations.dto.NotificationPayload;
import com.andesstay.reservations.model.Reservation;
import com.andesstay.reservations.producer.ReservationMessageProducer;
import com.andesstay.reservations.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Listar todas las reservas (GET)
    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Obtener una reserva por ID (GET)
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear una nueva reserva (POST)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation createReservation(@RequestBody Reservation reservation) {
        if (reservation.getStatus() == null || reservation.getStatus().isEmpty()) {
            reservation.setStatus("CREADA");
        }
        Reservation savedReservation = reservationRepository.save(reservation);

        // Envío asíncrono a RabbitMQ: Notificación de reserva creada
        NotificationPayload payload = new NotificationPayload(
                savedReservation.getId(),
                "contacto@huesped.cl",
                "Huesped AndesStay",
                "UNIDAD-1",
                savedReservation.getStatus(),
                "Su reserva #" + savedReservation.getId() + " ha sido recibida con exito en estado CREADA."
        );
        messageProducer.sendEmailNotification(payload);

        return savedReservation;
    }

    // Cambiar estado de reserva (PUT) -> Endpoint requerido en Seccion 5 del Caso
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body("El campo status es requerido");
        }

        return reservationRepository.findById(id).map(reservation -> {
            // Regla de negocio Seccion 3: No se puede hacer check-in sin estar CONFIRMADA
            if (("CHECKIN_PENDIENTE".equalsIgnoreCase(newStatus) || "EN_ESTADIA".equalsIgnoreCase(newStatus))
                    && !"CONFIRMADA".equalsIgnoreCase(reservation.getStatus())
                    && !"CHECKIN_PENDIENTE".equalsIgnoreCase(reservation.getStatus())) {
                return ResponseEntity.badRequest().body("Regla de negocio: No se puede hacer check-in sin estar CONFIRMADA.");
            }

            reservation.setStatus(newStatus.toUpperCase());
            Reservation updated = reservationRepository.save(reservation);

            NotificationPayload payload = new NotificationPayload(
                    updated.getId(),
                    "contacto@huesped.cl",
                    "Huesped AndesStay",
                    "UNIDAD-1",
                    updated.getStatus(),
                    "Actualizacion de reserva #" + updated.getId() + ": " + updated.getStatus()
            );

            // Si se confirma, dispara Email, Housekeeping y Voucher segun la topologia
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

    // Eliminar una reserva por ID (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}