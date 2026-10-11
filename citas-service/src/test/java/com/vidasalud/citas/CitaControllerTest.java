package com.vidasalud.citas;

import com.vidasalud.citas.client.PacienteDto;
import com.vidasalud.citas.client.PacientesClient;
import com.vidasalud.citas.dto.CitaResponse;
import com.vidasalud.citas.event.CitaEventoPublisher;
import com.vidasalud.citas.exception.RecursoNoEncontradoException;
import com.vidasalud.citas.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CitaControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    CitaRepository repository;

    @MockBean
    PacientesClient pacientesClient;
    @MockBean
    CitaEventoPublisher publisher;

    @BeforeEach
    void preparar() {
        repository.deleteAll().block();
        when(pacientesClient.obtener(1L)).thenReturn(Mono.just(new PacienteDto(1L, "ACTIVO")));
        when(pacientesClient.obtener(99L))
                .thenReturn(Mono.error(new RecursoNoEncontradoException("El paciente 99 no existe")));
        when(publisher.publicar(any())).thenReturn(Mono.empty());
    }

    private Map<String, Object> cita(long pacienteId, String profesional, int dias) {
        String fechaHora = LocalDateTime.now().plusDays(dias).withSecond(0).withNano(0).toString();
        return Map.of(
                "pacienteId", pacienteId,
                "especialidad", "Cardiologia",
                "fechaHora", fechaHora,
                "profesionalCodigo", profesional,
                "motivo", "Control administrativo");
    }

    private Long crearCita(String profesional, int dias) {
        CitaResponse creada = webTestClient.post().uri("/api/citas")
                .bodyValue(cita(1L, profesional, dias))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CitaResponse.class)
                .returnResult().getResponseBody();
        return creada.id();
    }

    @Test
    void registraYConsultaUnaCita() {
        Long id = crearCita("MED-100", 3);

        webTestClient.get().uri("/api/citas/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.estado").isEqualTo("PENDIENTE")
                .jsonPath("$.pacienteId").isEqualTo(1);
    }

    @Test
    void noRegistraCitaSiElPacienteNoExiste() {
        webTestClient.post().uri("/api/citas").bodyValue(cita(99L, "MED-101", 3))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void rechazaCitaDuplicadaDelMismoProfesionalEnElMismoHorario() {
        Map<String, Object> body = cita(1L, "MED-102", 4);
        webTestClient.post().uri("/api/citas").bodyValue(body).exchange().expectStatus().isCreated();

        webTestClient.post().uri("/api/citas").bodyValue(body).exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void confirmaCitaPendiente() {
        Long id = crearCita("MED-103", 5);

        webTestClient.patch().uri("/api/citas/{id}/estado", id)
                .bodyValue(Map.of("estado", "CONFIRMADA"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.estado").isEqualTo("CONFIRMADA");
    }

    @Test
    void transicionNoPermitidaDevuelve409() {
        Long id = crearCita("MED-104", 6);
        webTestClient.patch().uri("/api/citas/{id}/estado", id)
                .bodyValue(Map.of("estado", "CANCELADA")).exchange().expectStatus().isOk();

        webTestClient.patch().uri("/api/citas/{id}/estado", id)
                .bodyValue(Map.of("estado", "CONFIRMADA"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void citaInexistenteDevuelve404() {
        webTestClient.get().uri("/api/citas/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound();
    }
}
