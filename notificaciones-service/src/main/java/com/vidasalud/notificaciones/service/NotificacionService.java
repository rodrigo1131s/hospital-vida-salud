package com.vidasalud.notificaciones.service;

import com.vidasalud.notificaciones.event.CitaEvento;
import com.vidasalud.notificaciones.model.Notificacion;
import com.vidasalud.notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository repository;

    /** Simula el aviso guardandolo en BD. No registra datos personales en logs, solo ids. */
    public Mono<Notificacion> registrar(CitaEvento evento) {
        Notificacion notificacion = Notificacion.builder()
                .citaId(evento.citaId())
                .pacienteId(evento.pacienteId())
                .tipo("CITA_" + evento.estadoNuevo())
                .mensaje(construirMensaje(evento))
                .canal("SIMULADO")
                .fechaEnvio(LocalDateTime.now())
                .build();
        return repository.save(notificacion)
                .doOnNext(n -> log.info("Notificacion registrada tipo={} citaId={}", n.getTipo(), n.getCitaId()));
    }

    public Flux<Notificacion> listar(Long pacienteId) {
        return pacienteId == null ? repository.findAll() : repository.findByPacienteId(pacienteId);
    }

    private String construirMensaje(CitaEvento e) {
        return switch (e.estadoNuevo()) {
            case "PENDIENTE" -> "Su cita " + e.citaId() + " fue registrada.";
            case "CONFIRMADA" -> "Su cita " + e.citaId() + " fue confirmada.";
            case "CANCELADA" -> "Su cita " + e.citaId() + " fue cancelada.";
            case "ATENDIDA" -> "Su cita " + e.citaId() + " fue atendida.";
            case "AUSENTE" -> "Su cita " + e.citaId() + " fue marcada como ausente.";
            default -> "Cambio de estado en su cita " + e.citaId() + ".";
        };
    }
}
