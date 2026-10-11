package com.vidasalud.notificaciones.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {
    @Id
    private Long id;
    private Long citaId;
    private Long pacienteId;
    private String tipo;
    private String mensaje;
    private String canal; // SIMULADO: no se envian mensajes reales
    private LocalDateTime fechaEnvio;
}
