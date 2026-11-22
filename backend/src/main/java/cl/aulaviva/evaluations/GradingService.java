package cl.aulaviva.evaluations;

import cl.aulaviva.evaluations.dto.QuestionFeedback;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Correccion automatica con escala chilena 1,0 a 7,0 y exigencia de 60 %. */
@Service
public class GradingService {

    static final double PASSING_RATIO = 0.6;

    /** Corrige: selected = id de pregunta -> indice de alternativa elegida. */
    public GradingResult grade(Evaluation evaluation, Map<String, Integer> selected) {
        int score = 0;
        int max = 0;
        List<QuestionFeedback> feedback = new ArrayList<>();
        for (Question question : evaluation.getQuestions()) {
            max += question.getPoints();
            Integer choice = selected.get(question.getId());
            Optional<QuestionOption> correct = question.correctOption();
            boolean ok = choice != null && correct.isPresent()
                && correct.get().getPosition() == choice;
            if (ok) {
                score += question.getPoints();
            }
            feedback.add(new QuestionFeedback(question.getId(), ok,
                message(ok, choice != null, correct, question.getExplanation())));
        }
        return new GradingResult(score, max, toGrade(score, max), feedback);
    }

    /** Nota 1,0 a 7,0 con 60 % de exigencia, redondeada a un decimal. */
    public static BigDecimal toGrade(int score, int max) {
        double ratio = max <= 0 ? 0 : (double) score / max;
        double grade = ratio >= PASSING_RATIO
            ? 4.0 + 3.0 * (ratio - PASSING_RATIO) / (1 - PASSING_RATIO)
            : 1.0 + 3.0 * ratio / PASSING_RATIO;
        return BigDecimal.valueOf(grade).setScale(1, RoundingMode.HALF_UP);
    }

    private static String message(boolean ok, boolean answered, Optional<QuestionOption> correct,
            String explanation) {
        String base;
        if (ok) {
            base = "Correcta.";
        } else {
            String right = correct.map(o -> "\"" + o.getText() + "\"").orElse("(sin definir)");
            base = (answered ? "Incorrecta" : "Sin responder") + ": la respuesta correcta era "
                + right + ".";
        }
        return explanation == null || explanation.isBlank() ? base : base + " " + explanation;
    }
}
