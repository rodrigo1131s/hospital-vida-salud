package com.vidasalud.citas;

import com.vidasalud.citas.event.CitaEvento;
import com.vidasalud.citas.event.CitaEventoPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CitaEventoPublisherTest {

    @Mock
    RabbitTemplate rabbitTemplate;
    @InjectMocks
    CitaEventoPublisher publisher;

    private CitaEvento evento() {
        return new CitaEvento(10L, 1L, "PENDIENTE", "CONFIRMADA", LocalDateTime.now());
    }

    @Test
    void publicaEnElExchangeConLaRoutingKeyDelEstado() {
        CitaEvento evento = evento();

        publisher.publicar(evento).block();

        verify(rabbitTemplate).convertAndSend(eq("citas.exchange"), eq("cita.confirmada"), eq(evento));
    }

    @Test
    void siElBrokerFallaNoSePropagaElErrorAlFlujoDeNegocio() {
        doThrow(new AmqpException("broker caido"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(CitaEvento.class));

        StepVerifier.create(publisher.publicar(evento())).verifyComplete();
    }
}
