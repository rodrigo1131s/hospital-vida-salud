package com.vidasalud.pacientes.dto;

import com.vidasalud.pacientes.model.Paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(
        Long id,
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String telefono,
        String correo,
        String direccion,
        LocalDateTime fechaRegistro,
        String estado) {

    public static PacienteResponse from(Paciente p) {
        return new PacienteResponse(p.getId(), p.getTipoDocumento(), p.getNumeroDocumento(), p.getNombres(),
                p.getApellidos(), p.getFechaNacimiento(), p.getTelefono(), p.getCorreo(), p.getDireccion(),
                p.getFechaRegistro(), p.getEstado());
    }
}
