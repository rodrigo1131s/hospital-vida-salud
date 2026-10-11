package com.vidasalud.auditoria;

import com.vidasalud.auditoria.event.CitaEvento;
import com.vidasalud.auditoria.model.AuditoriaEvento;
import com.vidasalud.auditoria.repository.AuditoriaRepository;
import com.vidasalud.auditoria.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    AuditoriaRepository repository;
    @InjectMocks
    AuditoriaService service;

    @Test
    void registraEstadoAnteriorEstadoNuevoYOrigen() {
        when(repository.save(any(AuditoriaEvento.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        LocalDateTime fecha = LocalDateTime.now();
        CitaEvento evento = new CitaEvento(10L, 1L, "PENDIENTE", "CONFIRMADA", fecha);

        StepVerifier.create(service.registrar(evento))
                .assertNext(a -> {
                    assertEquals("CITA", a.getRecurso());
                    assertEquals(Long.valueOf(10L), a.getRecursoId());
                    assertEquals("PENDIENTE", a.getEstadoAnterior());
                    assertEquals("CONFIRMADA", a.getEstadoNuevo());
                    assertEquals("citas-service", a.getOrigen());
                    assertEquals(fecha, a.getFechaEvento());
                })
                .verifyComplete();
    }
}
