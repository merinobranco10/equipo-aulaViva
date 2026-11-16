package cl.aulaviva.evaluations;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GradingServiceTest {

    private final GradingService grading = new GradingService();

    @Test
    void todoCorrectoDaNota7() {
        assertThat(GradingService.toGrade(6, 6)).isEqualByComparingTo("7.0");
    }

    @Test
    void sesentaPorCientoDaNota4() {
        assertThat(GradingService.toGrade(6, 10)).isEqualByComparingTo("4.0");
    }

    @Test
    void ceroDaNota1() {
        assertThat(GradingService.toGrade(0, 10)).isEqualByComparingTo("1.0");
    }

    @Test
    void sinPuntajeMaximoDaNota1() {
        assertThat(GradingService.toGrade(0, 0)).isEqualByComparingTo("1.0");
    }

    @Test
    void dosDeTresDaNota4Coma5() {
        assertThat(GradingService.toGrade(4, 6)).isEqualByComparingTo("4.5");
    }

    @Test
    void corrigeConPuntajeYFeedback() {
        Evaluation evaluation = evaluacion();
        GradingResult result = grading.grade(evaluation, Map.of("q1", 1, "q2", 0));

        assertThat(result.score()).isEqualTo(2);
        assertThat(result.maxScore()).isEqualTo(5);
        assertThat(result.feedback()).hasSize(3);
        assertThat(result.feedback().get(0).correct()).isTrue();
        assertThat(result.feedback().get(0).message()).startsWith("Correcta.");
        assertThat(result.feedback().get(1).correct()).isFalse();
        assertThat(result.feedback().get(1).message()).contains("Incorrecta").contains("\"B\"")
            .contains("Explicación 2");
        assertThat(result.feedback().get(2).message()).startsWith("Sin responder");
    }

    private static Evaluation evaluacion() {
        Evaluation evaluation = new Evaluation("e1", "t1", "c1", "Prueba", "CREATED",
            Instant.now(), null);
        evaluation.addQuestion(pregunta("q1", 0, 2, 1, null));
        evaluation.addQuestion(pregunta("q2", 1, 2, 1, "Explicación 2"));
        evaluation.addQuestion(pregunta("q3", 2, 1, 0, ""));
        return evaluation;
    }

    private static Question pregunta(String id, int position, int points, int correct,
            String explanation) {
        Question question = new Question(id, "Pregunta " + id, points, position, explanation);
        question.addOption(new QuestionOption(id + "-a", "A", 0, correct == 0));
        question.addOption(new QuestionOption(id + "-b", "B", 1, correct == 1));
        return question;
    }
}
