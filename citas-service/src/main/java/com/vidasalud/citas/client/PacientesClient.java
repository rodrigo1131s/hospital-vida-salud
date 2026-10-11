package com.vidasalud.citas.client;

import com.vidasalud.citas.exception.RecursoNoEncontradoException;
import com.vidasalud.citas.exception.ServicioNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Comunicacion SINCRONA Citas -> Pacientes (se necesita la respuesta antes de crear la cita).
 * Contrato: GET /api/pacientes/{id} -> 200 {id, estado} | 404 si no existe.
 * Timeout: 3 s. Si Pacientes no responde -> 503 (ServicioNoDisponibleException).
 */
@Component
public class PacientesClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient webClient;

    public PacientesClient(@LoadBalanced WebClient.Builder builder,
                           @Value("${clients.pacientes.url:http://pacientes-service}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<PacienteDto> obtener(Long id) {
        return webClient.get()
                .uri("/api/pacientes/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> Mono.error(new RecursoNoEncontradoException("El paciente " + id + " no existe")))
                .bodyToMono(PacienteDto.class)
                .timeout(TIMEOUT)
                .onErrorMap(e -> !(e instanceof RecursoNoEncontradoException),
                        e -> new ServicioNoDisponibleException("El servicio de pacientes no esta disponible", e));
    }
}
