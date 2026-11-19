package com.andesstay.reservations.producer;

import com.andesstay.reservations.dto.ReservationStreamEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaReservationProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaReservationProducer producer;

    @Test
    void publishReservationEvent_DeberiaEnviarAAmbosTopicosConDatosCompletos() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        producer.publishReservationEvent(
                "RESERVA_CREADA",
                10L,
                "Juan Perez",
                "CABANA-VIP",
                "CREADA",
                "juan@correo.cl",
                "Reserva creada por web"
        );

        verify(kafkaTemplate, times(1)).send(
                eq("andesstay-reservas-streaming"),
                eq("10"),
                any(ReservationStreamEvent.class)
        );
        verify(kafkaTemplate, times(1)).send(
                eq("andesstay-auditoria-eventos"),
                eq("10"),
                any(ReservationStreamEvent.class)
        );
    }

    @Test
    void publishReservationEvent_DeberiaUsarValoresPorDefectoCuandoSonNull() {
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        producer.publishReservationEvent(
                "RESERVA_CONFIRMADA",
                null,
                "Maria Lopez",
                null,
                "CONFIRMADA",
                null,
                "Confirmación automática"
        );

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1)).send(
                eq("andesstay-reservas-streaming"),
                keyCaptor.capture(),
                any(ReservationStreamEvent.class)
        );

        assertNotNull(keyCaptor.getValue());
        assertFalse(keyCaptor.getValue().isBlank());
    }

    @Test
    void publishReservationEvent_NoDeberiaLanzarExcepcionSiFallaKafka() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Broker no disponible"));

        assertDoesNotThrow(() -> producer.publishReservationEvent(
                "RESERVA_CANCELADA",
                5L,
                "Carlos Ruiz",
                "HAB-101",
                "CANCELADA",
                "carlos@correo.cl",
                "Cancelada por usuario"
        ));
    }
}