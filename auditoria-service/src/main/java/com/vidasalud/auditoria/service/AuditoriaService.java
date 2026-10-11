package com.vidasalud.auditoria.service;

import com.vidasalud.auditoria.event.CitaEvento;
import com.vidasalud.auditoria.model.AuditoriaEvento;
import com.vidasalud.auditoria.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final AuditoriaRepository repository;

    /** Trazabilidad: fecha, recurso, estado anterior, estado nuevo y origen del cambio. */
    public Mono<AuditoriaEvento> registrar(CitaEvento evento) {
        AuditoriaEvento auditoria = AuditoriaEvento.builder()
                .recurso("CITA")
                .recursoId(evento.citaId())
                .estadoAnterior(evento.estadoAnterior())
                .estadoNuevo(evento.estadoNuevo())
                .origen("citas-service")
                .fechaEvento(evento.fecha())
                .build();
        return repository.save(auditoria)
                .doOnNext(a -> log.info("Auditoria registrada recurso={} id={} {} -> {}",
                        a.getRecurso(), a.getRecursoId(), a.getEstadoAnterior(), a.getEstadoNuevo()));
    }

    public Flux<AuditoriaEvento> listar(Long recursoId) {
        return recursoId == null ? repository.findAll() : repository.findByRecursoId(recursoId);
    }
}
