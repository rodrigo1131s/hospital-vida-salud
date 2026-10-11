package com.vidasalud.citas.client;

/** Contrato minimo que Citas necesita de Pacientes (id y estado). */
public record PacienteDto(Long id, String estado) {
}
