package com.vidasalud.auditoria.repository;

import com.vidasalud.auditoria.model.AuditoriaEvento;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AuditoriaRepository extends ReactiveCrudRepository<AuditoriaEvento, Long> {

    Flux<AuditoriaEvento> findByRecursoId(Long recursoId);
}
