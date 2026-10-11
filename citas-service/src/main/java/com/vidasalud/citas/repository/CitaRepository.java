package com.vidasalud.citas.repository;

import com.vidasalud.citas.model.Cita;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;

public interface CitaRepository extends ReactiveCrudRepository<Cita, Long> {

    Flux<Cita> findByPacienteId(Long pacienteId);

    Mono<Boolean> existsByProfesionalCodigoAndFechaHoraAndEstadoIn(String profesionalCodigo,
                                                                     LocalDateTime fechaHora,
                                                                     Collection<String> estados);
}
