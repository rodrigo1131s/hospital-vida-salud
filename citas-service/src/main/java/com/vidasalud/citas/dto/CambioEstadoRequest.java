package com.vidasalud.citas.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioEstadoRequest(@NotBlank String estado) {
}
