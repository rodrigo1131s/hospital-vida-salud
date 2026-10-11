package com.vidasalud.pacientes.controller;

import com.vidasalud.pacientes.dto.PacienteActualizacionRequest;
import com.vidasalud.pacientes.dto.PacienteRequest;
import com.vidasalud.pacientes.dto.PacienteResponse;
import com.vidasalud.pacientes.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PacienteResponse> registrar(@Valid @RequestBody PacienteRequest request) {
        return service.registrar(request);
    }

    @GetMapping("/{id}")
    public Mono<PacienteResponse> obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public Flux<PacienteResponse> listar() {
        return service.listar();
    }

    @PutMapping("/{id}")
    public Mono<PacienteResponse> actualizar(@PathVariable Long id,
                                             @Valid @RequestBody PacienteActualizacionRequest request) {
        return service.actualizar(id, request);
    }
}
