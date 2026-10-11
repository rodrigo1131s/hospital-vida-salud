package com.vidasalud.auditoria.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("auditoria_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaEvento {
    @Id
    private Long id;
    private String recurso;
    private Long recursoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String origen;
    private LocalDateTime fechaEvento;
}
