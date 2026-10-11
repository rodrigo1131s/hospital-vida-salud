package com.vidasalud.citas.service;

import com.vidasalud.citas.client.PacientesClient;
import com.vidasalud.citas.dto.CitaRequest;
import com.vidasalud.citas.dto.CitaResponse;
import com.vidasalud.citas.event.CitaEvento;
import com.vidasalud.citas.event.CitaEventoPublisher;
import com.vidasalud.citas.exception.ConflictoException;
import com.vidasalud.citas.exception.RecursoNoEncontradoException;
import com.vidasalud.citas.exception.SolicitudInvalidaException;
import com.vidasalud.citas.model.Cita;
import com.vidasalud.citas.model.EstadoCita;
import com.vidasalud.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    private static final List<String> ESTADOS_ACTIVOS =
            List.of(EstadoCita.PENDIENTE.name(), EstadoCita.CONFIRMADA.name());

    private final CitaRepository repository;
    private final PacientesClient pacientesClient;
    private final CitaEventoPublisher publisher;

    /** 1) valida paciente (sincrono) 2) valida agenda 3) guarda 4) publica evento. */
    public Mono<CitaResponse> crear(CitaRequest req) {
        return pacientesClient.obtener(req.pacienteId())
                .flatMap(paciente -> {
                    if (!"ACTIVO".equals(paciente.estado())) {
                        return Mono.<Boolean>error(new ConflictoException("El paciente esta INACTIVO"));
                    }
                    return repository.existsByProfesionalCodigoAndFechaHoraAndEstadoIn(
                            req.profesionalCodigo(), req.fechaHora(), ESTADOS_ACTIVOS);
                })
                .flatMap(ocupado -> ocupado
                        ? Mono.<Cita>error(new ConflictoException(
                                "El profesional " + req.profesionalCodigo() + " ya tiene una cita activa en ese horario"))
                        : repository.save(nuevaCita(req)))
                .flatMap(cita -> publicar(cita, null).thenReturn(cita))
                .map(CitaResponse::from);
    }

    public Mono<CitaResponse> obtener(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Cita " + id + " no encontrada")))
                .map(CitaResponse::from);
    }

    public Flux<CitaResponse> listar(Long pacienteId) {
        Flux<Cita> citas = pacienteId == null ? repository.findAll() : repository.findByPacienteId(pacienteId);
        return citas.map(CitaResponse::from);
    }

    /** Valida la transicion (ej. una cita CANCELADA no puede CONFIRMARSE) y publica el evento. */
    public Mono<CitaResponse> cambiarEstado(Long id, String estadoDestino) {
        return Mono.fromCallable(() -> parsearEstado(estadoDestino))
                .flatMap(destino -> repository.findById(id)
                        .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Cita " + id + " no encontrada")))
                        .flatMap(cita -> {
                            EstadoCita actual = EstadoCita.valueOf(cita.getEstado());
                            if (!actual.puedePasarA(destino)) {
                                return Mono.<Cita>error(new ConflictoException(
                                        "Transicion no permitida: " + actual + " -> " + destino));
                            }
                            cita.setEstado(destino.name());
                            cita.setFechaActualizacion(LocalDateTime.now());
                            return repository.save(cita)
                                    .flatMap(guardada -> publicar(guardada, actual.name()).thenReturn(guardada));
                        }))
                .map(CitaResponse::from);
    }

    private EstadoCita parsearEstado(String valor) {
        try {
            return EstadoCita.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SolicitudInvalidaException("Estado invalido: " + valor);
        }
    }

    private Cita nuevaCita(CitaRequest req) {
        LocalDateTime ahora = LocalDateTime.now();
        return Cita.builder()
                .pacienteId(req.pacienteId())
                .especialidad(req.especialidad())
                .fechaHora(req.fechaHora())
                .profesionalCodigo(req.profesionalCodigo())
                .motivo(req.motivo())
                .estado(EstadoCita.PENDIENTE.name())
                .fechaCreacion(ahora)
                .fechaActualizacion(ahora)
                .build();
    }

    private Mono<Void> publicar(Cita cita, String estadoAnterior) {
        return publisher.publicar(new CitaEvento(cita.getId(), cita.getPacienteId(), estadoAnterior,
                cita.getEstado(), LocalDateTime.now()));
    }
}
