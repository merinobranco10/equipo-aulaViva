package cl.aulaviva.tutor;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Normalización compartida por BM25 y caché. */
final class Text {

    private static final Set<String> STOPWORDS = Set.of(
        "a", "al", "algo", "ante", "como", "con", "contra",
        "cual", "cuando", "de", "del", "desde", "donde",
        "el", "ella", "ellas", "ellos", "en", "entre",
        "era", "es", "esa", "ese", "eso", "esta", "este",
        "esto", "fue", "ha", "han", "hasta", "hay",
        "la", "las", "le", "les", "lo", "los",
        "mas", "me", "mi", "mis", "muy",
        "o", "para", "pero", "por", "porque",
        "que", "quien", "se", "sin", "sobre",
        "su", "sus", "te", "tu", "tus",
        "un", "una", "uno", "unos", "unas",
        "y", "ya"
    );

    private Text() {}

    static List<String> terms(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);

        return Arrays.stream(normalized.split("[^\\p{L}\\p{N}]+"))
            .filter(token -> !token.isBlank())
            .filter(token -> !STOPWORDS.contains(token))
            .toList();
    }

    static String key(String value) {
        return String.join(" ", terms(value));
    }
}