package com.vidasalud.notificaciones;

import com.vidasalud.notificaciones.event.CitaEvento;
import com.vidasalud.notificaciones.messaging.CitaEventoListener;
import com.vidasalud.notificaciones.model.Notificacion;
import com.vidasalud.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Prueba del componente de mensajeria: el consumidor procesa el evento recibido. */
@ExtendWith(MockitoExtension.class)
class CitaEventoListenerTest {

    @Mock
    NotificacionService service;
    @InjectMocks
    CitaEventoListener listener;

    @Test
    void alRecibirElEventoLoDelegaAlServicioDeNotificaciones() {
        CitaEvento evento = new CitaEvento(10L, 1L, "PENDIENTE", "CONFIRMADA", LocalDateTime.now());
        when(service.registrar(evento)).thenReturn(Mono.just(new Notificacion()));

        listener.onCitaEvento(evento);

        verify(service).registrar(evento);
    }
}
