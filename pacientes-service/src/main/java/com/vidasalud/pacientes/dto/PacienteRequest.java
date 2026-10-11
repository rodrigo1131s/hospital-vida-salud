package com.vidasalud.pacientes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteRequest(
        @NotBlank String tipoDocumento,
        @NotBlank @Size(max = 20) String numeroDocumento,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotNull @Past LocalDate fechaNacimiento,
        String telefono,
        @Email String correo,
        String direccion) {
}
