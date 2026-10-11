package com.vidasalud.notificaciones.controller;

import com.vidasalud.notificaciones.model.Notificacion;
import com.vidasalud.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService service;

    @GetMapping
    public Flux<Notificacion> listar(@RequestParam(required = false) Long pacienteId) {
        return service.listar(pacienteId);
    }
}
