package com.vidasalud.citas.controller;

import com.vidasalud.citas.dto.CambioEstadoRequest;
import com.vidasalud.citas.dto.CitaRequest;
import com.vidasalud.citas.dto.CitaResponse;
import com.vidasalud.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CitaResponse> crear(@Valid @RequestBody CitaRequest request) {
        return service.crear(request);
    }

    @GetMapping("/{id}")
    public Mono<CitaResponse> obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping
    public Flux<CitaResponse> listar(@RequestParam(required = false) Long pacienteId) {
        return service.listar(pacienteId);
    }

    @PatchMapping("/{id}/estado")
    public Mono<CitaResponse> cambiarEstado(@PathVariable Long id,
                                            @Valid @RequestBody CambioEstadoRequest request) {
        return service.cambiarEstado(id, request.estado());
    }
}
