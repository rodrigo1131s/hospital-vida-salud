package com.vidasalud.notificaciones.messaging;

import com.vidasalud.notificaciones.config.RabbitConfig;
import com.vidasalud.notificaciones.event.CitaEvento;
import com.vidasalud.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitaEventoListener {

    private final NotificacionService service;

    /**
     * Corre en un hilo del contenedor AMQP (no en un hilo de Netty), por eso el block() final
     * es aceptable: asegura que el mensaje solo se confirma (ack) tras persistir en la BD.
     */
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onCitaEvento(CitaEvento evento) {
        log.info("Evento recibido estado={} citaId={}", evento.estadoNuevo(), evento.citaId());
        service.registrar(evento).block();
    }
}
