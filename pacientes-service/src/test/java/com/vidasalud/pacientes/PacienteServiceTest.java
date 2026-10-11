package com.vidasalud.pacientes;

import com.vidasalud.pacientes.dto.PacienteRequest;
import com.vidasalud.pacientes.exception.ConflictoException;
import com.vidasalud.pacientes.exception.RecursoNoEncontradoException;
import com.vidasalud.pacientes.model.Paciente;
import com.vidasalud.pacientes.repository.PacienteRepository;
import com.vidasalud.pacientes.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    PacienteRepository repository;

    @InjectMocks
    PacienteService service;

    private PacienteRequest request() {
        return new PacienteRequest("DNI", "12345678", "Ana", "Prueba Ficticia",
                LocalDate.of(1990, 5, 20), "999000111", "ana@ejemplo.test", null);
    }

    @Test
    void registraPacienteValido() {
        when(repository.existsByTipoDocumentoAndNumeroDocumento("DNI", "12345678")).thenReturn(Mono.just(false));
        when(repository.save(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            p.setId(1L);
            return Mono.just(p);
        });

        StepVerifier.create(service.registrar(request()))
                .assertNext(r -> {
                    assertEquals(Long.valueOf(1L), r.id());
                    assertEquals("INACTIVO", r.estado());
                })
                .verifyComplete();
    }

    @Test
    void rechazaPacienteDuplicado() {
        when(repository.existsByTipoDocumentoAndNumeroDocumento("DNI", "12345678")).thenReturn(Mono.just(true));

        StepVerifier.create(service.registrar(request()))
                .expectError(ConflictoException.class)
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void consultaDePacienteInexistenteDevuelveNoEncontrado() {
        when(repository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(service.obtener(99L))
                .expectError(RecursoNoEncontradoException.class)
                .verify();
    }
}
