package cl.aulaviva.tutor;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Reglas de seguridad del tutor: bloquear ataques y revisar la respuesta. */
@Component
public final class Guardrails {

    public static final int LARGO_MAXIMO = 500;

    // Respuesta fija cuando la pregunta no está en los apuntes
    public static final String MSG_FUERA_DE_ALCANCE =
        "No encuentro eso en tus apuntes. Pregúntale a tu docente.";

    // Respuesta fija cuando se bloquea un ataque
    public static final String MSG_BLOQUEADO =
        "No puedo ayudarte con eso. Pregúntame algo de los apuntes de tu curso.";

    // Frases de ataque. Se buscan en el texto en minúsculas y SIN tildes.
    private static final List<Pattern> ATAQUES = List.of(
        Pattern.compile("ignora (todas )?(las |tus )?(instrucciones|reglas)"),
        Pattern.compile("ignore (all |previous |the )?instructions"),
        Pattern.compile("olvida (todo|tus reglas|las reglas|las instrucciones)"),
        Pattern.compile("system prompt|mensaje de sistema|prompt secreto|tu prompt"),
        Pattern.compile("actua como|haz de cuenta|jailbreak|modo desarrollador"),
        Pattern.compile("(notas|datos|rut|correo)s? de (todos|otros|mi compa)"),
        Pattern.compile("nota (saco|tiene|tuvo|obtuvo) "),
        Pattern.compile("</?apuntes>"),
        Pattern.compile("\\bsystem\\s*:"),
        Pattern.compile("api key|clave del proveedor|clave de la api")
    );

    // "DAN" se busca aparte y en mayúsculas, porque "dan" es una palabra normal
    private static final Pattern DAN = Pattern.compile("\\bDAN\\b");

    // Busca [fuente: nombre.md] en la respuesta
    private static final Pattern FUENTE = Pattern.compile("\\[fuente: ([^\\]]+)\\]");

    public Guardrails() {}

    /** Contrato de entrada consumido por TutorService. */
    public Optional<String> inputViolation(String pregunta) {
        return debeBloquear(pregunta) ? Optional.of("injection") : Optional.empty();
    }

    /** Contrato de salida: la cita debe corresponder a una fuente recuperada. */
    public boolean citesRetrievedSource(String respuesta, Collection<String> fuentes) {
        return citaFuenteValida(respuesta, fuentes);
    }

    /** Quita tildes y pasa a minúsculas. */
    private static String limpiar(String s) {
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase();
    }

    /** Sirve para bloquear: pregunta muy larga o con frases de ataque. */
    public static boolean debeBloquear(String pregunta) {
        if (pregunta == null) {
            return false;
        }
        if (pregunta.length() > LARGO_MAXIMO) {
            return true;
        }
        if (DAN.matcher(pregunta).find()) {
            return true;
        }
        String texto = limpiar(pregunta);
        for (Pattern p : ATAQUES) {
            if (p.matcher(texto).find()) {
                return true;
            }
        }
        return false;
    }

    /** La respuesta es válida si cita una fuente que sí fue recuperada. */
    public static boolean citaFuenteValida(String respuesta, Collection<String> fuentes) {
        if (respuesta == null) {
            return false;
        }
        Matcher m = FUENTE.matcher(respuesta);
        while (m.find()) {
            if (fuentes.contains(m.group(1).trim())) {
                return true;
            }
        }
        return false;
    }
}
