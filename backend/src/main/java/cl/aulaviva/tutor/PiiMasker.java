package cl.aulaviva.tutor;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Enmascara RUT, correo y teléfono antes de enviar texto al proveedor externo. */
@Component
public class PiiMasker {
    private static final Pattern RUT =
        Pattern.compile("\\b\\d{1,2}\\.?\\d{3}\\.?\\d{3}-[\\dkK]\\b");
    private static final Pattern CORREO =
        Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");
    private static final Pattern TELEFONO =
        Pattern.compile("(?<!\\d)(\\+?56\\s?)?9\\s?\\d{4}\\s?\\d{4}(?!\\d)");

    public record Resultado(String texto, boolean huboDatos) {}

    public Resultado maskWithResult(String texto) {
        if (texto == null) return new Resultado("", false);
        String t = RUT.matcher(texto).replaceAll("[RUT]");
        t = CORREO.matcher(t).replaceAll("[CORREO]");
        t = TELEFONO.matcher(t).replaceAll("[TELEFONO]");
        return new Resultado(t, !t.equals(texto));
    }

    public String mask(String texto) {
        return maskWithResult(texto).texto();
    }

    /** API estática conservada para las pruebas y llamadas originales de Gerardo. */
    public static Resultado tapar(String texto) {
        return new PiiMasker().maskWithResult(texto);
    }
}
