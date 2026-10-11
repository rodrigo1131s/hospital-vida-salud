package com.vidasalud.pacientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PacienteActualizacionRequest(
        @NotBlank String nombres,
        @NotBlank String apellidos,
        String telefono,
        @Email String correo,
        String direccion,
        @Pattern(regexp = "ACTIVO|INACTIVO", message = "debe ser ACTIVO o INACTIVO") String estado) {
}
