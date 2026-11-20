package cl.aulaviva.tutor;

import cl.aulaviva.tutor.Bm25Index.Chunk;
import cl.aulaviva.tutor.Bm25Index.Hit;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Lee classpath:corpus/&lt;courseId&gt;/*.md al iniciar, parte cada apunte en secciones "##" y
 * arma un índice BM25 por curso. El curso (y su colegio) ya fue validado antes de buscar.
 */
@Component
public class CorpusIndex {

    private static final Logger LOG = LoggerFactory.getLogger(CorpusIndex.class);
    private static final Pattern LOCATION = Pattern.compile("corpus/([^/]+)/([^/]+\\.md)$");

    private final Map<String, Bm25Index> byCourse;

    public CorpusIndex() throws IOException {
        Resource[] files =
            new PathMatchingResourcePatternResolver().getResources("classpath*:corpus/*/*.md");
        Arrays.sort(files, Comparator.comparing(Resource::getFilename,
            Comparator.nullsFirst(Comparator.naturalOrder())));
        Map<String, List<Chunk>> chunks = new TreeMap<>();
        for (Resource file : files) {
            Matcher matcher = LOCATION.matcher(file.getURL().toString());
            if (!matcher.find()) {
                continue;
            }
            String markdown = file.getContentAsString(StandardCharsets.UTF_8);
            chunks.computeIfAbsent(matcher.group(1), k -> new ArrayList<>())
                .addAll(parse(matcher.group(2), markdown));
        }
        Map<String, Bm25Index> index = new HashMap<>();
        chunks.forEach((course, list) -> {
            index.put(course, new Bm25Index(list));
            LOG.info("Corpus del curso {}: {} fragmentos", course, list.size());
        });
        this.byCourse = Map.copyOf(index);
    }

    public boolean hasCourse(String courseId) {
        return byCourse.containsKey(courseId);
    }

    /** Top-k fragmentos del curso; lista vacía si el curso no tiene apuntes. */
    public List<Hit> search(String courseId, String query, int k) {
        Bm25Index index = byCourse.get(courseId);
        return index == null ? List.of() : index.search(query, k);
    }

    /** Una sección "##" = un fragmento. El encabezado inicial (metadatos) no se indexa. */
    static List<Chunk> parse(String source, String markdown) {
        List<Chunk> chunks = new ArrayList<>();
        String heading = null;
        StringBuilder body = new StringBuilder();
        for (String line : markdown.split("\\R")) {
            if (line.startsWith("## ")) {
                addChunk(chunks, source, heading, body);
                heading = line.substring(3).trim();
                body = new StringBuilder();
            } else if (heading != null) {
                body.append(line).append(' ');
            }
        }
        addChunk(chunks, source, heading, body);
        return chunks;
    }

    private static void addChunk(List<Chunk> chunks, String source, String heading,
            StringBuilder body) {
        String content = body.toString().replaceAll("\\s+", " ").trim();
        if (heading == null || content.isEmpty()) {
            return;
        }
        String text = heading + ". " + content;
        chunks.add(new Chunk(source + "#" + chunks.size(), source, text, Text.terms(text)));
    }
}
