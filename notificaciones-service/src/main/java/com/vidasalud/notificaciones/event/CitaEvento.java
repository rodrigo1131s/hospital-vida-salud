package com.vidasalud.notificaciones.event;

import java.time.LocalDateTime;

/** Copia local del contrato del evento (el consumidor no depende del codigo de Citas). */
public record CitaEvento(Long citaId,
                         Long pacienteId,
                         String estadoAnterior,
                         String estadoNuevo,
                         LocalDateTime fecha) {
}
