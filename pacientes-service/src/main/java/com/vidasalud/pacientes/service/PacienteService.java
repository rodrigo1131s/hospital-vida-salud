package com.vidasalud.pacientes.service;

import com.vidasalud.pacientes.dto.PacienteActualizacionRequest;
import com.vidasalud.pacientes.dto.PacienteRequest;
import com.vidasalud.pacientes.dto.PacienteResponse;
import com.vidasalud.pacientes.exception.ConflictoException;
import com.vidasalud.pacientes.exception.RecursoNoEncontradoException;
import com.vidasalud.pacientes.model.Paciente;
import com.vidasalud.pacientes.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository repository;

    /** Regla: no se repite (tipoDocumento, numeroDocumento). Mono porque devuelve 0 o 1 resultado. */
    public Mono<PacienteResponse> registrar(PacienteRequest req) {
        return repository.existsByTipoDocumentoAndNumeroDocumento(req.tipoDocumento(), req.numeroDocumento())
                .flatMap(existe -> existe
                        ? Mono.<Paciente>error(new ConflictoException(
                                "Ya existe un paciente con documento " + req.tipoDocumento() + " " + req.numeroDocumento()))
                        : repository.save(aEntidad(req)))
                // Respaldo ante una carrera: la restriccion UNIQUE de la BD tambien protege
                .onErrorMap(DataIntegrityViolationException.class,
                        e -> new ConflictoException("Paciente duplicado (documento ya registrado)"))
                .map(PacienteResponse::from);
    }

    public Mono<PacienteResponse> obtener(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Paciente " + id + " no encontrado")))
                .map(PacienteResponse::from);
    }

    /** Flux porque devuelve una secuencia de resultados. */
    public Flux<PacienteResponse> listar() {
        return repository.findAll().map(PacienteResponse::from);
    }

    public Mono<PacienteResponse> actualizar(Long id, PacienteActualizacionRequest req) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Paciente " + id + " no encontrado")))
                .flatMap(p -> {
                    p.setNombres(req.nombres());
                    p.setApellidos(req.apellidos());
                    p.setTelefono(req.telefono());
                    p.setCorreo(req.correo());
                    p.setDireccion(req.direccion());
                    if (req.estado() != null) {
                        p.setEstado(req.estado());
                    }
                    return repository.save(p);
                })
                .map(PacienteResponse::from);
    }

    private Paciente aEntidad(PacienteRequest req) {
        return Paciente.builder()
                .tipoDocumento(req.tipoDocumento())
                .numeroDocumento(req.numeroDocumento())
                .nombres(req.nombres())
                .apellidos(req.apellidos())
                .fechaNacimiento(req.fechaNacimiento())
                .telefono(req.telefono())
                .correo(req.correo())
                .direccion(req.direccion())
                .fechaRegistro(LocalDateTime.now())
                .estado("ACTIVO")
                .build();
    }
}
