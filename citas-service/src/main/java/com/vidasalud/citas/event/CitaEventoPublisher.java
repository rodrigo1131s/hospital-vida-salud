package com.vidasalud.citas.event;

import com.vidasalud.citas.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Publica el evento en RabbitMQ. Routing key: cita.<estado> (ej. cita.confirmada).
 * El envio es E/S bloqueante, por eso se aisla en boundedElastic para no frenar el pipeline reactivo.
 * Si el broker cae, el cambio de estado ya guardado no se revierte (limitacion documentada: seria un patron Outbox).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CitaEventoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public Mono<Void> publicar(CitaEvento evento) {
        String routingKey = "cita." + evento.estadoNuevo().toLowerCase();
        return Mono.fromRunnable(() -> {
                    rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, evento);
                    log.info("Evento publicado routingKey={} citaId={}", routingKey, evento.citaId());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.warn("No se pudo publicar el evento citaId={}: {}", evento.citaId(), e.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
