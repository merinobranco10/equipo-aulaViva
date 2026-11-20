package cl.aulaviva.tutor;

import cl.aulaviva.academic.AcademicDirectory;
import cl.aulaviva.academic.Course;
import cl.aulaviva.academic.CourseRepository;
import cl.aulaviva.identity.AulaVivaUser;
import cl.aulaviva.shared.NotFoundException;
import cl.aulaviva.shared.TooManyRequestsException;
import cl.aulaviva.tutor.Bm25Index.Hit;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Flujo de una pregunta al tutor (plan Día 4): inscripción, guardrail de entrada, PII,
 * presupuesto, BM25, umbral, caché, LLM, guardrail de salida y respuesta. Si el LLM falla o no
 * hay clave, responde en modo extractivo. En los logs nunca va el texto de la pregunta.
 */
@Service
public class TutorService {

    private static final Logger LOG = LoggerFactory.getLogger(TutorService.class);

    static final String NO_INFO = "No encuentro eso en tus apuntes. Pregúntale a tu docente.";
    static final String BLOCKED =
        "No puedo ayudarte con esa solicitud. Pregunta por contenidos de tu curso.";
    static final String MODE_LLM = "llm";
    static final String MODE_CACHE = "cache";
    static final String MODE_EXTRACTIVE = "extractivo";
    static final String MODE_BLOCKED = "bloqueado";
    static final String GUARDRAIL_PII = "pii";
    static final String GUARDRAIL_OUT_OF_SCOPE = "fuera_alcance";
    private static final int MAX_EXTRACT_WORDS = 120;

    private final CourseRepository courses;
    private final AcademicDirectory directory;
    private final CorpusIndex corpus;
    private final Guardrails guardrails;
    private final PiiMasker piiMasker;
    private final TutorPrompt prompt;
    private final LlmClient llm;
    private final TutorBudget budget;
    private final TutorCache cache;
    private final TutorProperties props;
    private final MeterRegistry metrics;

    public TutorService(CourseRepository courses, AcademicDirectory directory,
            CorpusIndex corpus, Guardrails guardrails, PiiMasker piiMasker, TutorPrompt prompt,
            LlmClient llm, TutorBudget budget, TutorCache cache, TutorProperties props,
            MeterRegistry metrics) {
        this.courses = courses;
        this.directory = directory;
        this.corpus = corpus;
        this.guardrails = guardrails;
        this.piiMasker = piiMasker;
        this.prompt = prompt;
        this.llm = llm;
        this.budget = budget;
        this.cache = cache;
        this.props = props;
        this.metrics = metrics;
    }

    /** Datos de la llamada para métricas y log (nunca el texto de la pregunta). */
    private record Call(String tenant, String courseId, int length, String hash,
            long startNanos) { }

    public TutorAnswerResponse ask(AulaVivaUser student, TutorQuestionRequest request) {
        long start = System.nanoTime();
        String tenant = student.tenantId();

        // 1. Inscripción: un curso de otro colegio o ajeno responde 404.
        Course course = courses.findByIdAndTenantId(request.courseId(), tenant)
            .filter(c -> directory.isEnrolled(c.getId(), student.id()))
            .orElseThrow(() -> new NotFoundException("El curso no existe."));

        String question = request.question().trim();
        PiiMasker.Resultado pii = piiMasker.maskWithResult(question);
        String masked = pii.texto();
        Call call = new Call(tenant, course.getId(), question.length(), shortHash(masked), start);

        // 2. Guardrail de entrada: no se llama al LLM.
        Optional<String> violation = guardrails.inputViolation(question);
        if (violation.isPresent()) {
            guardrail(tenant, violation.get());
            return done(call, MODE_BLOCKED, violation.get(), BLOCKED, List.of(), 0, 0);
        }

        // 3. PII enmascarada: se sigue con la pregunta limpia.
        if (pii.huboDatos()) {
            guardrail(tenant, GUARDRAIL_PII);
        }

        // 4. Presupuesto diario del colegio.
        if (budget.isExhausted(tenant)) {
            throw new TooManyRequestsException("Presupuesto diario del colegio agotado.");
        }

        // 5 y 6. Recuperación BM25 y umbral de relevancia.
        List<Hit> hits = corpus.search(course.getId(), masked, props.topK());
        if (hits.isEmpty() || hits.get(0).score() < props.minScore()) {
            guardrail(tenant, GUARDRAIL_OUT_OF_SCOPE);
            return done(call, MODE_BLOCKED, GUARDRAIL_OUT_OF_SCOPE, NO_INFO, List.of(), 0, 0);
        }
        List<String> sources = hits.stream().map(h -> h.chunk().source()).distinct().toList();

        // 7. Caché.
        Optional<TutorCache.Cached> cached = cache.get(tenant, course.getId(), masked);
        if (cached.isPresent()) {
            return done(call, MODE_CACHE, null, cached.get().answer(), cached.get().sources(),
                0, 0);
        }

        // 8. LLM (sin clave o con falla: modo extractivo).
        if (!llm.enabled()) {
            return extractive(call, hits, 0, 0);
        }
        LlmAnswer answer;
        try {
            answer = llm.complete(prompt.system(course.getName(), hits), prompt.user(masked));
        } catch (Exception e) {
            LOG.warn("LLM no disponible ({}); se responde en modo extractivo",
                e.getClass().getSimpleName());
            return extractive(call, hits, 0, 0);
        }
        budget.add(tenant, answer.totalTokens());

        // 9. Guardrail de salida: PII enmascarada y fuente citada entre las recuperadas.
        String text = piiMasker.mask(answer.content());
        if (text.toLowerCase(java.util.Locale.ROOT).contains("no encuentro eso en tus apuntes")) {
            guardrail(tenant, GUARDRAIL_OUT_OF_SCOPE);
            return done(call, MODE_LLM, GUARDRAIL_OUT_OF_SCOPE, NO_INFO, List.of(),
                answer.promptTokens(), answer.completionTokens());
        }
        if (!guardrails.citesRetrievedSource(text, sources)) {
            return extractive(call, hits, answer.promptTokens(), answer.completionTokens());
        }

        // 10. Respuesta.
        cache.put(tenant, course.getId(), masked, new TutorCache.Cached(text, sources));
        return done(call, MODE_LLM, null, text, sources, answer.promptTokens(),
            answer.completionTokens());
    }

    private TutorAnswerResponse extractive(Call call, List<Hit> hits, int promptTokens,
            int completionTokens) {
        Hit best = hits.get(0);
        String source = best.chunk().source();
        String excerpt = limitWords(best.chunk().text(), MAX_EXTRACT_WORDS);
        return done(call, MODE_EXTRACTIVE, null, excerpt + " [fuente: " + source + "]",
            List.of(source), promptTokens, completionTokens);
    }

    private void guardrail(String tenant, String type) {
        metrics.counter("tutor.guardrail", "tenant", tenant, "type", type).increment();
    }

    private TutorAnswerResponse done(Call call, String mode, String guardrail, String answer,
            List<String> sources, int promptTokens, int completionTokens) {
        long nanos = System.nanoTime() - call.startNanos();
        metrics.counter("tutor.requests", "tenant", call.tenant(), "mode", mode).increment();
        if (promptTokens > 0) {
            metrics.counter("tutor.tokens", "tenant", call.tenant(), "type", "prompt")
                .increment(promptTokens);
        }
        if (completionTokens > 0) {
            metrics.counter("tutor.tokens", "tenant", call.tenant(), "type", "completion")
                .increment(completionTokens);
        }
        metrics.timer("tutor.latency", "mode", mode).record(nanos, TimeUnit.NANOSECONDS);
        LOG.info("tutor tenant={} course={} len={} hash={} mode={} guardrail={} "
                + "promptTokens={} completionTokens={} ms={}",
            call.tenant(), call.courseId(), call.length(), call.hash(), mode,
            guardrail == null ? "-" : guardrail, promptTokens, completionTokens,
            TimeUnit.NANOSECONDS.toMillis(nanos));
        return new TutorAnswerResponse(answer, sources, mode, guardrail);
    }

    static String limitWords(String text, int maxWords) {
        String[] words = text.trim().split("\\s+");
        return words.length <= maxWords ? text.trim()
            : String.join(" ", Arrays.copyOfRange(words, 0, maxWords)) + "…";
    }

    /** Identificador corto de la pregunta (ya sin PII) para correlacionar logs sin guardarla. */
    static String shortHash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
