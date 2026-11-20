package cl.aulaviva.tutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PiiMaskerTest {

    @Test
    void tapaElRut() {
        var r = PiiMasker.tapar("Mi RUT es 12.345.678-9, ayuda con potencias");
        assertEquals("Mi RUT es [RUT], ayuda con potencias", r.texto());
        assertTrue(r.huboDatos());
    }

    @Test
    void tapaCorreoYTelefono() {
        var r = PiiMasker.tapar("Escríbeme a ana@correo.cl o al +56 9 1234 5678");
        assertEquals("Escríbeme a [CORREO] o al [TELEFONO]", r.texto());
        assertTrue(r.huboDatos());
    }

    @Test
    void dejaIgualUnTextoSinDatos() {
        var r = PiiMasker.tapar("¿Cuánto es (-3) elevado a 2?");
        assertEquals("¿Cuánto es (-3) elevado a 2?", r.texto());
        assertFalse(r.huboDatos());
    }
}
