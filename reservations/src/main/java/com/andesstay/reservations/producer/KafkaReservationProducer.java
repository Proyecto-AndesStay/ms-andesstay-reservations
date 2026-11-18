package com.andesstay.reservations.producer;

import com.andesstay.reservations.dto.ReservationStreamEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaReservationProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaReservationProducer.class);

    private static final String TOPIC_STREAMING = "andesstay-reservas-streaming";
    private static final String TOPIC_AUDITORIA = "andesstay-auditoria-eventos";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaReservationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReservationEvent(String eventType, Long reservationId, String guestName,
                                        String roomType, String status, String userEmail, String details) {
        try {
            ReservationStreamEvent event = new ReservationStreamEvent(
                    UUID.randomUUID().toString(),
                    eventType,
                    reservationId,
                    guestName,
                    roomType != null ? roomType : "UNIDAD-1",
                    status,
                    userEmail != null ? userEmail : "admin_andesstay@proyectotest.onmicrosoft.com",
                    details
            );

            String key = reservationId != null ? String.valueOf(reservationId) : UUID.randomUUID().toString();

            kafkaTemplate.send(TOPIC_STREAMING, key, event).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA-STREAMING] Evento enviado a {} | Partition: {} | Offset: {}",
                            TOPIC_STREAMING,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA-ERROR] Fallo al enviar a {}: {}", TOPIC_STREAMING, ex.getMessage());
                }
            });

            kafkaTemplate.send(TOPIC_AUDITORIA, key, event).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[KAFKA-AUDIT] Evento enviado a {} | Partition: {} | Offset: {}",
                            TOPIC_AUDITORIA,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("[KAFKA-ERROR] Fallo al enviar a {}: {}", TOPIC_AUDITORIA, ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("[KAFKA-EXCEPTION] Error controlado al publicar en Kafka: {}", e.getMessage());
        }
    }
}