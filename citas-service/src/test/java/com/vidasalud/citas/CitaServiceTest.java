package com.vidasalud.citas;

import com.vidasalud.citas.client.PacienteDto;
import com.vidasalud.citas.client.PacientesClient;
import com.vidasalud.citas.dto.CitaRequest;
import com.vidasalud.citas.event.CitaEvento;
import com.vidasalud.citas.event.CitaEventoPublisher;
import com.vidasalud.citas.exception.ConflictoException;
import com.vidasalud.citas.exception.RecursoNoEncontradoException;
import com.vidasalud.citas.exception.SolicitudInvalidaException;
import com.vidasalud.citas.model.Cita;
import com.vidasalud.citas.repository.CitaRepository;
import com.vidasalud.citas.service.CitaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    CitaRepository repository;
    @Mock
    PacientesClient pacientesClient;
    @Mock
    CitaEventoPublisher publisher;
    @InjectMocks
    CitaService service;

    private CitaRequest request() {
        return new CitaRequest(1L, "Medicina General", LocalDateTime.now().plusDays(2), "MED-001",
                "Control administrativo");
    }

    private Cita cita(String estado) {
        return Cita.builder().id(10L).pacienteId(1L).especialidad("Medicina General")
                .fechaHora(LocalDateTime.now().plusDays(2)).profesionalCodigo("MED-001").estado(estado)
                .fechaCreacion(LocalDateTime.now()).fechaActualizacion(LocalDateTime.now()).build();
    }

    @Test
    void creaCitaPendienteSiElPacienteExiste() {
        when(pacientesClient.obtener(1L)).thenReturn(Mono.just(new PacienteDto(1L, "ACTIVO")));
        when(repository.existsByProfesionalCodigoAndFechaHoraAndEstadoIn(eq("MED-001"), any(), anyCollection()))
                .thenReturn(Mono.just(false));
        when(repository.save(any(Cita.class))).thenAnswer(inv -> {
            Cita c = inv.getArgument(0);
            c.setId(10L);
            return Mono.just(c);
        });
        when(publisher.publicar(any(CitaEvento.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.crear(request()))
                .assertNext(r -> {
                    assertEquals("PENDIENTE", r.estado());
                    assertEquals(Long.valueOf(10L), r.id());
                })
                .verifyComplete();
    }

    @Test
    void noCreaCitaSiElPacienteNoExiste() {
        when(pacientesClient.obtener(1L)).thenReturn(Mono.error(new RecursoNoEncontradoException("no existe")));

        StepVerifier.create(service.crear(request()))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void noCreaCitaSiElProfesionalYaTieneCitaEnEseHorario() {
        when(pacientesClient.obtener(1L)).thenReturn(Mono.just(new PacienteDto(1L, "ACTIVO")));
        when(repository.existsByProfesionalCodigoAndFechaHoraAndEstadoIn(eq("MED-001"), any(), anyCollection()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.crear(request()))
                .expectError(ConflictoException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void confirmarCitaPendientePublicaElEventoCitaConfirmada() {
        when(repository.findById(10L)).thenReturn(Mono.just(cita("PENDIENTE")));
        when(repository.save(any(Cita.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(publisher.publicar(any(CitaEvento.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.cambiarEstado(10L, "CONFIRMADA"))
                .assertNext(r -> assertEquals("CONFIRMADA", r.estado()))
                .verifyComplete();

        ArgumentCaptor<CitaEvento> captor = ArgumentCaptor.forClass(CitaEvento.class);
        verify(publisher).publicar(captor.capture());
        assertEquals("PENDIENTE", captor.getValue().estadoAnterior());
        assertEquals("CONFIRMADA", captor.getValue().estadoNuevo());
    }

    @Test
    void noPermiteConfirmarUnaCitaCancelada() {
        when(repository.findById(10L)).thenReturn(Mono.just(cita("CANCELADA")));

        StepVerifier.create(service.cambiarEstado(10L, "CONFIRMADA"))
                .expectError(ConflictoException.class)
                .verify();

        verify(repository, never()).save(any());
        verify(publisher, never()).publicar(any());
    }

    @Test
    void cambiarEstadoDeCitaInexistenteDevuelveNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.cambiarEstado(99L, "CONFIRMADA"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();
    }

    @Test
    void estadoInexistenteEsSolicitudInvalida() {
        StepVerifier.create(service.cambiarEstado(10L, "VOLANDO"))
                .expectError(SolicitudInvalidaException.class)
                .verify();
    }
}
