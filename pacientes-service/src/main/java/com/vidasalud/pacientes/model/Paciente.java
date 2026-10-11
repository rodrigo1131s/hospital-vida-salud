package com.vidasalud.pacientes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {
    @Id
    private Long id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String direccion;
    private LocalDateTime fechaRegistro;
    private String estado; // ACTIVO | INACTIVO
}
