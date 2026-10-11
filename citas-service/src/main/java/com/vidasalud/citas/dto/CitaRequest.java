package com.vidasalud.citas.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CitaRequest(
        @NotNull Long pacienteId,
        @NotBlank String especialidad,
        @NotNull @Future LocalDateTime fechaHora,
        @NotBlank String profesionalCodigo,
        @Size(max = 200) String motivo) {
}
