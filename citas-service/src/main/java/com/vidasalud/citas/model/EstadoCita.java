package com.vidasalud.citas.model;

/** Estados de la cita y transiciones permitidas. */
public enum EstadoCita {
    PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, AUSENTE;

    public boolean puedePasarA(EstadoCita destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == ATENDIDA || destino == CANCELADA || destino == AUSENTE;
            case ATENDIDA, CANCELADA, AUSENTE -> false;
        };
    }
}
