package com.vidasalud.auditoria.messaging;

import com.vidasalud.auditoria.config.RabbitConfig;
import com.vidasalud.auditoria.event.CitaEvento;
import com.vidasalud.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitaEventoListener {

    private final AuditoriaService service;

    /** Hilo del contenedor AMQP (no reactivo): block() asegura persistir antes de confirmar el mensaje. */
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onCitaEvento(CitaEvento evento) {
        log.info("Evento recibido estado={} citaId={}", evento.estadoNuevo(), evento.citaId());
        service.registrar(evento).block();
    }
}
