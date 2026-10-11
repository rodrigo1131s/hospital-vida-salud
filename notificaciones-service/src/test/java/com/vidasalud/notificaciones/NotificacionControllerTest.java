package com.vidasalud.notificaciones;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NotificacionControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void listaVaciaAlInicio() {
        webTestClient.get().uri("/api/notificaciones")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
    }
}
