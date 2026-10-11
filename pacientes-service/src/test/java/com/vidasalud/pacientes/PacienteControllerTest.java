package com.vidasalud.pacientes;

import com.vidasalud.pacientes.dto.PacienteResponse;
import com.vidasalud.pacientes.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PacienteControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PacienteRepository repository;

    @BeforeEach
    void limpiar() {
        repository.deleteAll().block();
    }

    private Map<String, Object> body(String numeroDocumento) {
        return Map.of(
                "tipoDocumento", "DNI",
                "numeroDocumento", numeroDocumento,
                "nombres", "Luis",
                "apellidos", "Ficticio Prueba",
                "fechaNacimiento", "1985-03-10",
                "telefono", "999111222",
                "correo", "luis@ejemplo.test");
    }

    @Test
    void registraYConsultaPaciente() {
        PacienteResponse creado = webTestClient.post().uri("/api/pacientes")
                .bodyValue(body("70000001"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PacienteResponse.class)
                .returnResult().getResponseBody();

        webTestClient.get().uri("/api/pacientes/{id}", creado.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.numeroDocumento").isEqualTo("70000001")
                .jsonPath("$.estado").isEqualTo("ACTIVO");
    }

    @Test
    void rechazaPacienteDuplicadoConConflicto() {
        webTestClient.post().uri("/api/pacientes").bodyValue(body("70000002"))
                .exchange().expectStatus().isCreated();

        webTestClient.post().uri("/api/pacientes").bodyValue(body("70000002"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void pacienteInexistenteDevuelve404() {
        webTestClient.get().uri("/api/pacientes/{id}", 9999)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void datosInvalidosDevuelven400() {
        webTestClient.post().uri("/api/pacientes")
                .bodyValue(Map.of("tipoDocumento", "DNI", "numeroDocumento", "70000003"))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
