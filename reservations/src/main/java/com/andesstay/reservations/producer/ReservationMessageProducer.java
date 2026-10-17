package com.andesstay.reservations.producer;

import com.andesstay.reservations.dto.MessageEnvelope;
import com.andesstay.reservations.dto.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReservationMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(ReservationMessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${andesstay.rabbitmq.exchanges.direct}")
    private String directExchange;

    @Value("${andesstay.rabbitmq.routing-keys.email-send}")
    private String emailRoutingKey;

    @Value("${andesstay.rabbitmq.routing-keys.housekeeping-ticket}")
    private String housekeepingRoutingKey;

    @Value("${andesstay.rabbitmq.routing-keys.voucher-gen}")
    private String voucherRoutingKey;

    public ReservationMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // 1. Notificación al Huésped (q.cmd.email)
    public void sendEmailNotification(NotificationPayload payload) {
        MessageEnvelope<NotificationPayload> envelope = new MessageEnvelope<>("EMAIL_NOTIFICATION", payload);
        log.info("Publicando en RabbitMQ [Email]: EventId={}, ReservaId={}", envelope.getEventId(), payload.getReservationId());
        rabbitTemplate.convertAndSend(directExchange, emailRoutingKey, envelope);
    }

    // 2. Ticket para Housekeeping / Limpieza (q.cmd.housekeeping)
    public void sendHousekeepingTicket(NotificationPayload payload) {
        MessageEnvelope<NotificationPayload> envelope = new MessageEnvelope<>("HOUSEKEEPING_TICKET", payload);
        log.info("Publicando en RabbitMQ [Housekeeping]: EventId={}, Unidad={}", envelope.getEventId(), payload.getUnitId());
        rabbitTemplate.convertAndSend(directExchange, housekeepingRoutingKey, envelope);
    }

    // 3. Generación de Voucher / PDF (q.cmd.voucher)
    public void sendVoucherGeneration(NotificationPayload payload) {
        MessageEnvelope<NotificationPayload> envelope = new MessageEnvelope<>("VOUCHER_GENERATION", payload);
        log.info("Publicando en RabbitMQ [Voucher]: EventId={}, ReservaId={}", envelope.getEventId(), payload.getReservationId());
        rabbitTemplate.convertAndSend(directExchange, voucherRoutingKey, envelope);
    }
}