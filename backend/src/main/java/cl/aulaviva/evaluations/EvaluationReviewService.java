package cl.aulaviva.evaluations;

import cl.aulaviva.academic.AcademicDirectory;
import cl.aulaviva.academic.CourseSummary;
import cl.aulaviva.evaluations.dto.MyEvaluationItem;
import cl.aulaviva.evaluations.dto.QuestionFeedback;
import cl.aulaviva.evaluations.dto.ReviewQuestion;
import cl.aulaviva.evaluations.dto.SubmissionReviewResponse;
import cl.aulaviva.identity.AppUser;
import cl.aulaviva.identity.AulaVivaUser;
import cl.aulaviva.progress.ProgressService;
import cl.aulaviva.shared.ForbiddenException;
import cl.aulaviva.shared.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultas de solo lectura: revisión de las respuestas marcadas por el propio estudiante y
 * listado de evaluaciones de un estudiante para su apoderado, docente o coordinador. Todo se
 * filtra por el tenant del usuario autenticado (ADR 0007).
 */
@Service
public class EvaluationReviewService {

    private final EvaluationRepository evaluations;
    private final SubmissionRepository submissions;
    private final SubmissionAnswerRepository answers;
    private final GradeRepository grades;
    private final AcademicDirectory directory;
    private final ProgressService progress;
    private final ObjectMapper objectMapper;

    public EvaluationReviewService(EvaluationRepository evaluations,
            SubmissionRepository submissions, SubmissionAnswerRepository answers,
            GradeRepository grades, AcademicDirectory directory, ProgressService progress,
            ObjectMapper objectMapper) {
        this.evaluations = evaluations;
        this.submissions = submissions;
        this.answers = answers;
        this.grades = grades;
        this.directory = directory;
        this.progress = progress;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public SubmissionReviewResponse myReview(AulaVivaUser student, String evaluationId) {
        Evaluation evaluation = evaluations.findByIdAndTenantId(evaluationId, student.tenantId())
            .orElseThrow(() -> new NotFoundException("La evaluación no existe."));
        if (!directory.isEnrolled(evaluation.getCourseId(), student.id())) {
            throw new ForbiddenException("No estás inscrito en el curso de esta evaluación.");
        }
        Submission submission =
            submissions.findByEvaluationIdAndStudentId(evaluation.getId(), student.id())
                .orElseThrow(() -> new NotFoundException("Aún no has rendido esta evaluación."));
        Grade grade = grades.findBySubmissionId(submission.getId())
            .orElseThrow(() -> new IllegalStateException("Entrega sin nota: " + submission.getId()));

        Map<String, String> optionByQuestion = answers.findBySubmissionId(submission.getId())
            .stream().filter(a -> a.getOptionId() != null)
            .collect(Collectors.toMap(SubmissionAnswer::getQuestionId,
                SubmissionAnswer::getOptionId));
        Map<String, QuestionFeedback> feedbackByQuestion = parseFeedback(grade.getFeedback())
            .stream().collect(Collectors.toMap(QuestionFeedback::questionId,
                Function.identity()));

        List<ReviewQuestion> questions = evaluation.getQuestions().stream()
            .map(q -> toReview(q, optionByQuestion.get(q.getId()),
                feedbackByQuestion.get(q.getId())))
            .toList();
        return new SubmissionReviewResponse(evaluation.getId(), evaluation.getTitle(),
            grade.getGrade(), grade.getScoreObtained(), grade.getMaxScore(), questions);
    }

    /** Evaluaciones del estudiante con su estado y nota, si el usuario puede ver al estudiante. */
    @Transactional(readOnly = true)
    public List<MyEvaluationItem> evaluationsOfStudent(AulaVivaUser viewer, String studentId) {
        AppUser student = progress.authorizedStudent(viewer, studentId);
        List<CourseSummary> courses = directory.coursesOfStudent(student.getId());
        if (courses.isEmpty()) {
            return List.of();
        }
        Map<String, String> courseNames = courses.stream()
            .collect(Collectors.toMap(CourseSummary::id, CourseSummary::name));
        Map<String, Submission> mine =
            submissions.findByTenantIdAndStudentId(viewer.tenantId(), student.getId()).stream()
                .collect(Collectors.toMap(Submission::getEvaluationId, Function.identity()));
        return evaluations
            .findByTenantIdAndCourseIdInOrderByCreatedAtDesc(viewer.tenantId(),
                courseNames.keySet())
            .stream()
            .map(e -> {
                Submission s = mine.get(e.getId());
                BigDecimal grade = s == null ? null
                    : grades.findBySubmissionId(s.getId()).map(Grade::getGrade).orElse(null);
                return new MyEvaluationItem(e.getId(), e.getTitle(), e.getCourseId(),
                    courseNames.get(e.getCourseId()), s == null ? "PENDING" : "COMPLETED", grade);
            })
            .toList();
    }

    private static ReviewQuestion toReview(Question question, String optionId,
            QuestionFeedback feedback) {
        Integer selected = optionId == null ? null
            : question.getOptions().stream().filter(o -> o.getId().equals(optionId))
                .map(QuestionOption::getPosition).findFirst().orElse(null);
        int correctIndex = question.correctOption().map(QuestionOption::getPosition).orElse(-1);
        return new ReviewQuestion(question.getId(), question.getText(),
            question.getOptions().stream().map(QuestionOption::getText).toList(),
            question.getPoints(), selected, correctIndex,
            feedback != null && feedback.correct(), feedback == null ? "" : feedback.message());
    }

    private List<QuestionFeedback> parseFeedback(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<QuestionFeedback>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Feedback guardado con formato inválido", e);
        }
    }
}
