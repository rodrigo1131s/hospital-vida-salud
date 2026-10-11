package com.vidasalud.auditoria.controller;

import com.vidasalud.auditoria.model.AuditoriaEvento;
import com.vidasalud.auditoria.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService service;

    @GetMapping
    public Flux<AuditoriaEvento> listar(@RequestParam(required = false) Long recursoId) {
        return service.listar(recursoId);
    }
}
