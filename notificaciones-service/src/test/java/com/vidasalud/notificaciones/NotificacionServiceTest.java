package com.vidasalud.notificaciones;

import com.vidasalud.notificaciones.event.CitaEvento;
import com.vidasalud.notificaciones.model.Notificacion;
import com.vidasalud.notificaciones.repository.NotificacionRepository;
import com.vidasalud.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    NotificacionRepository repository;
    @InjectMocks
    NotificacionService service;

    @Test
    void registraNotificacionCuandoLaCitaSeConfirma() {
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        CitaEvento evento = new CitaEvento(10L, 1L, "PENDIENTE", "CONFIRMADA", LocalDateTime.now());

        StepVerifier.create(service.registrar(evento))
                .assertNext(n -> {
                    assertEquals("CITA_CONFIRMADA", n.getTipo());
                    assertEquals(Long.valueOf(10L), n.getCitaId());
                    assertEquals("SIMULADO", n.getCanal());
                    assertTrue(n.getMensaje().contains("confirmada"));
                })
                .verifyComplete();
    }
}
