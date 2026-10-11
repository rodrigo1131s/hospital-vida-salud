package com.vidasalud.auditoria;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuditoriaControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void listaVaciaAlInicio() {
        webTestClient.get().uri("/api/auditoria")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
    }
}
