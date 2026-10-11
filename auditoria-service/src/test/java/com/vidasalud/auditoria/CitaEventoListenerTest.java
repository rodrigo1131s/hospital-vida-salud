package com.vidasalud.auditoria;

import com.vidasalud.auditoria.event.CitaEvento;
import com.vidasalud.auditoria.messaging.CitaEventoListener;
import com.vidasalud.auditoria.model.AuditoriaEvento;
import com.vidasalud.auditoria.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaEventoListenerTest {

    @Mock
    AuditoriaService service;
    @InjectMocks
    CitaEventoListener listener;

    @Test
    void alRecibirElEventoLoDelegaAlServicioDeAuditoria() {
        CitaEvento evento = new CitaEvento(10L, 1L, "PENDIENTE", "CONFIRMADA", LocalDateTime.now());
        when(service.registrar(evento)).thenReturn(Mono.just(new AuditoriaEvento()));

        listener.onCitaEvento(evento);

        verify(service).registrar(evento);
    }
}
