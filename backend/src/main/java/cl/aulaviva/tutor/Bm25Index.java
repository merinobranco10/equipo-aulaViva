package cl.aulaviva.tutor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Índice BM25 en memoria sobre los fragmentos de los apuntes de un curso. */
public final class Bm25Index {

    private static final double K1 = 1.2;
    private static final double B = 0.75;

    /** Fragmento de un apunte (una sección "##"). */
    public record Chunk(String id, String source, String text, List<String> terms) { }

    /** Fragmento recuperado con su puntaje. */
    public record Hit(Chunk chunk, double score) { }

    private final List<Chunk> chunks;
    private final List<Map<String, Integer>> termFrequencies = new ArrayList<>();
    private final Map<String, Integer> documentFrequency = new HashMap<>();
    private final double averageLength;

    public Bm25Index(List<Chunk> chunks) {
        this.chunks = List.copyOf(chunks);
        for (Chunk chunk : this.chunks) {
            Map<String, Integer> tf = new HashMap<>();
            chunk.terms().forEach(t -> tf.merge(t, 1, Integer::sum));
            termFrequencies.add(tf);
            tf.keySet().forEach(t -> documentFrequency.merge(t, 1, Integer::sum));
        }
        double avg = this.chunks.stream().mapToInt(c -> c.terms().size()).average().orElse(1);
        this.averageLength = avg <= 0 ? 1 : avg;
    }

    public int size() {
        return chunks.size();
    }

    /** Los k fragmentos más relevantes para la consulta (solo con puntaje mayor que cero). */
    public List<Hit> search(String query, int k) {
        List<String> queryTerms = Text.terms(query).stream().distinct().toList();
        int n = chunks.size();
        List<Hit> hits = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Chunk chunk = chunks.get(i);
            Map<String, Integer> tf = termFrequencies.get(i);
            double score = 0;
            for (String term : queryTerms) {
                Integer df = documentFrequency.get(term);
                int f = tf.getOrDefault(term, 0);
                if (df == null || f == 0) {
                    continue;
                }
                double idf = Math.log(1 + (n - df + 0.5) / (df + 0.5));
                double norm = f + K1 * (1 - B + B * chunk.terms().size() / averageLength);
                score += idf * (f * (K1 + 1)) / norm;
            }
            if (score > 0) {
                hits.add(new Hit(chunk, score));
            }
        }
        hits.sort(Comparator.comparingDouble(Hit::score).reversed());
        return hits.size() > k ? List.copyOf(hits.subList(0, Math.max(k, 0))) : hits;
    }
}
