package com.vidasalud.citas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("cita")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {
    @Id
    private Long id;
    /** Solo el identificador: el paciente vive en pacientes-service (sin clave foranea entre BDs). */
    private Long pacienteId;
    private String especialidad;
    private LocalDateTime fechaHora;
    private String profesionalCodigo;
    private String motivo;
    private String estado; // valor de EstadoCita
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
