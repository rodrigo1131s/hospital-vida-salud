package com.vidasalud.citas.event;

import java.time.LocalDateTime;

/** Evento de negocio: una cita cambio de estado. Sin datos clinicos ni personales. */
public record CitaEvento(Long citaId,
                         Long pacienteId,
                         String estadoAnterior,
                         String estadoNuevo,
                         LocalDateTime fecha) {
}
