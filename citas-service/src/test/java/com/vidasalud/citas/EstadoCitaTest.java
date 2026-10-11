package com.vidasalud.citas;

import com.vidasalud.citas.model.EstadoCita;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoCitaTest {

    @Test
    void pendientePuedeConfirmarseOCancelarse() {
        assertTrue(EstadoCita.PENDIENTE.puedePasarA(EstadoCita.CONFIRMADA));
        assertTrue(EstadoCita.PENDIENTE.puedePasarA(EstadoCita.CANCELADA));
    }

    @Test
    void pendienteNoPuedeMarcarseAtendida() {
        assertFalse(EstadoCita.PENDIENTE.puedePasarA(EstadoCita.ATENDIDA));
    }

    @Test
    void confirmadaPuedeAtenderseCancelarseOMarcarseAusente() {
        assertTrue(EstadoCita.CONFIRMADA.puedePasarA(EstadoCita.ATENDIDA));
        assertTrue(EstadoCita.CONFIRMADA.puedePasarA(EstadoCita.CANCELADA));
        assertTrue(EstadoCita.CONFIRMADA.puedePasarA(EstadoCita.AUSENTE));
    }

    @Test
    void canceladaNoPuedeConfirmarse() {
        assertFalse(EstadoCita.CANCELADA.puedePasarA(EstadoCita.CONFIRMADA));
    }

    @Test
    void estadosFinalesNoTienenTransiciones() {
        for (EstadoCita destino : EstadoCita.values()) {
            assertFalse(EstadoCita.ATENDIDA.puedePasarA(destino));
            assertFalse(EstadoCita.CANCELADA.puedePasarA(destino));
            assertFalse(EstadoCita.AUSENTE.puedePasarA(destino));
        }
    }
}
