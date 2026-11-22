package cl.aulaviva.evaluations;

import cl.aulaviva.academic.AcademicDirectory;
import cl.aulaviva.academic.Course;
import cl.aulaviva.academic.CourseRepository;
import cl.aulaviva.academic.CourseSummary;
import cl.aulaviva.evaluations.dto.AnswerInput;
import cl.aulaviva.evaluations.dto.CreateEvaluationRequest;
import cl.aulaviva.evaluations.dto.EvaluationCreatedResponse;
import cl.aulaviva.evaluations.dto.EvaluationResponse;
import cl.aulaviva.evaluations.dto.MyEvaluationItem;
import cl.aulaviva.evaluations.dto.QuestionFeedback;
import cl.aulaviva.evaluations.dto.QuestionInput;
import cl.aulaviva.evaluations.dto.QuestionResponse;
import cl.aulaviva.evaluations.dto.SubmissionRequest;
import cl.aulaviva.evaluations.dto.SubmissionResponse;
import cl.aulaviva.events.OutboxEvent;
import cl.aulaviva.events.OutboxEventRepository;
import cl.aulaviva.identity.AulaVivaUser;
import cl.aulaviva.identity.Role;
import cl.aulaviva.shared.ForbiddenException;
import cl.aulaviva.shared.NotFoundException;
import cl.aulaviva.shared.UnprocessableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de evaluaciones. Recibe el usuario autenticado explicitamente: todas las
 * busquedas se filtran por su tenant y un recurso de otro colegio responde 404.
 */
@Service
public class EvaluationService {

    public static final String EVENT_EVALUATION_COMPLETED = "EvaluationCompleted";

    private final EvaluationRepository evaluations;
    private final CourseRepository courses;
    private final SubmissionRepository submissions;
    private final SubmissionAnswerRepository answers;
    private final GradeRepository grades;
    private final OutboxEventRepository outbox;
    private final AcademicDirectory directory;
    private final GradingService grading;
    private final ObjectMapper objectMapper;

    public EvaluationService(EvaluationRepository evaluations, CourseRepository courses,
            SubmissionRepository submissions, SubmissionAnswerRepository answers,
            GradeRepository grades, OutboxEventRepository outbox, AcademicDirectory directory,
            GradingService grading, ObjectMapper objectMapper) {
        this.evaluations = evaluations;
        this.courses = courses;
        this.submissions = submissions;
        this.answers = answers;
        this.grades = grades;
        this.outbox = outbox;
        this.directory = directory;
        this.grading = grading;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EvaluationCreatedResponse create(AulaVivaUser teacher, CreateEvaluationRequest request,
            String idempotencyKey) {
        Optional<Evaluation> previous =
            evaluations.findByTenantIdAndIdempotencyKey(teacher.tenantId(), idempotencyKey);
        if (previous.isPresent()) {
            return toCreated(previous.get());
        }
        Course course = courses.findByIdAndTenantId(request.courseId(), teacher.tenantId())
            .orElseThrow(() -> new NotFoundException("El curso no existe."));
        if (!teacher.id().equals(course.getTeacherId())) {
            throw new ForbiddenException("Solo el docente del curso puede crear evaluaciones.");
        }
        Evaluation evaluation = new Evaluation(newId(), teacher.tenantId(), course.getId(),
            request.title().trim(), "CREATED", Instant.now(), idempotencyKey);
        int position = 0;
        for (QuestionInput input : request.questions()) {
            if (input.correctOption() >= input.options().size()) {
                throw new UnprocessableException("correctOption fuera de rango en la pregunta "
                    + (position + 1) + ".");
            }
            Question question = new Question(newId(), input.text().trim(), input.points(),
                position, input.explanation());
            for (int i = 0; i < input.options().size(); i++) {
                question.addOption(new QuestionOption(newId(), input.options().get(i).trim(), i,
                    i == input.correctOption()));
            }
            evaluation.addQuestion(question);
            position++;
        }
        evaluations.save(evaluation);
        return toCreated(evaluation);
    }

    @Transactional(readOnly = true)
    public EvaluationResponse get(AulaVivaUser user, String evaluationId) {
        Evaluation evaluation = load(user, evaluationId);
        checkCanView(user, evaluation);
        List<QuestionResponse> questions = evaluation.getQuestions().stream()
            .map(q -> new QuestionResponse(q.getId(), q.getText(),
                q.getOptions().stream().map(QuestionOption::getText).toList(), q.getPoints()))
            .toList();
        return new EvaluationResponse(evaluation.getId(), evaluation.getTitle(), questions);
    }

    @Transactional
    public SubmissionResponse submit(AulaVivaUser student, String evaluationId,
            SubmissionRequest request, String idempotencyKey) {
        Evaluation evaluation = load(student, evaluationId);
        if (!directory.isEnrolled(evaluation.getCourseId(), student.id())) {
            throw new ForbiddenException("No estás inscrito en el curso de esta evaluación.");
        }
        Optional<Submission> previous =
            submissions.findByEvaluationIdAndStudentId(evaluation.getId(), student.id());
        if (previous.isPresent()) {
            if (idempotencyKey.equals(previous.get().getIdempotencyKey())) {
                return replay(previous.get());
            }
            throw new UnprocessableException("Ya rendiste esta evaluación.");
        }
        Map<String, Integer> selected = validateAnswers(evaluation, request);
        GradingResult result = grading.grade(evaluation, selected);

        Submission submission = new Submission(newId(), student.tenantId(), evaluation.getId(),
            student.id(), Instant.now(), "COMPLETED", idempotencyKey);
        submissions.save(submission);
        Map<String, Question> byId = evaluation.getQuestions().stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));
        selected.forEach((questionId, index) -> {
            String optionId = byId.get(questionId).optionAt(index)
                .map(QuestionOption::getId).orElse(null);
            answers.save(new SubmissionAnswer(newId(), submission.getId(), questionId, optionId));
        });
        grades.save(new Grade(newId(), submission.getId(), result.grade(), result.score(),
            result.maxScore(), toJson(result.feedback())));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("evaluationId", evaluation.getId());
        payload.put("studentId", student.id());
        payload.put("grade", result.grade());
        outbox.save(new OutboxEvent(newId(), student.tenantId(), EVENT_EVALUATION_COMPLETED,
            toJson(payload), Instant.now()));

        return new SubmissionResponse(result.grade(), result.score(), result.maxScore(),
            "COMPLETED", result.feedback());
    }

    @Transactional(readOnly = true)
    public List<MyEvaluationItem> myEvaluations(AulaVivaUser user) {
        List<CourseSummary> myCourses = switch (user.role()) {
            case STUDENT -> directory.coursesOfStudent(user.id());
            case TEACHER -> courses.findByTenantIdAndTeacherIdOrderByName(user.tenantId(),
                    user.id()).stream()
                .map(c -> new CourseSummary(c.getId(), c.getName())).toList();
            default -> List.of();
        };
        if (myCourses.isEmpty()) {
            return List.of();
        }
        Map<String, String> courseNames = myCourses.stream()
            .collect(Collectors.toMap(CourseSummary::id, CourseSummary::name));
        Map<String, Submission> mine = user.role() == Role.STUDENT
            ? submissions.findByTenantIdAndStudentId(user.tenantId(), user.id()).stream()
                .collect(Collectors.toMap(Submission::getEvaluationId, Function.identity()))
            : Map.of();
        return evaluations
            .findByTenantIdAndCourseIdInOrderByCreatedAtDesc(user.tenantId(), courseNames.keySet())
            .stream()
            .map(e -> {
                String status = "CREATED";
                BigDecimal grade = null;
                if (user.role() == Role.STUDENT) {
                    Submission s = mine.get(e.getId());
                    status = s == null ? "PENDING" : "COMPLETED";
                    grade = s == null ? null
                        : grades.findBySubmissionId(s.getId()).map(Grade::getGrade).orElse(null);
                }
                return new MyEvaluationItem(e.getId(), e.getTitle(), e.getCourseId(),
                    courseNames.get(e.getCourseId()), status, grade);
            })
            .toList();
    }

    private Evaluation load(AulaVivaUser user, String evaluationId) {
        return evaluations.findByIdAndTenantId(evaluationId, user.tenantId())
            .orElseThrow(() -> new NotFoundException("La evaluación no existe."));
    }

    private void checkCanView(AulaVivaUser user, Evaluation evaluation) {
        boolean allowed = switch (user.role()) {
            case TEACHER -> courses.findByIdAndTenantId(evaluation.getCourseId(), user.tenantId())
                .map(c -> user.id().equals(c.getTeacherId())).orElse(false);
            case STUDENT -> directory.isEnrolled(evaluation.getCourseId(), user.id());
            case COORDINATOR -> true;
            case GUARDIAN -> false;
        };
        if (!allowed) {
            throw new ForbiddenException("No tienes acceso a esta evaluación.");
        }
    }

    private Map<String, Integer> validateAnswers(Evaluation evaluation,
            SubmissionRequest request) {
        Map<String, Question> byId = evaluation.getQuestions().stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));
        Map<String, Integer> selected = new LinkedHashMap<>();
        for (AnswerInput answer : request.answers()) {
            Question question = byId.get(answer.questionId());
            if (question == null) {
                throw new UnprocessableException(
                    "La pregunta " + answer.questionId() + " no pertenece a la evaluación.");
            }
            if (selected.containsKey(answer.questionId())) {
                throw new UnprocessableException(
                    "La pregunta " + answer.questionId() + " viene repetida.");
            }
            if (answer.selectedOption() >= question.getOptions().size()) {
                throw new UnprocessableException(
                    "selectedOption fuera de rango en la pregunta " + answer.questionId() + ".");
            }
            selected.put(answer.questionId(), answer.selectedOption());
        }
        return selected;
    }

    private SubmissionResponse replay(Submission submission) {
        Grade grade = grades.findBySubmissionId(submission.getId())
            .orElseThrow(() -> new IllegalStateException("Entrega sin nota: " + submission.getId()));
        List<QuestionFeedback> feedback = fromJson(grade.getFeedback());
        return new SubmissionResponse(grade.getGrade(), grade.getScoreObtained(),
            grade.getMaxScore(), submission.getStatus(), feedback);
    }

    private static EvaluationCreatedResponse toCreated(Evaluation evaluation) {
        return new EvaluationCreatedResponse(evaluation.getId(), evaluation.getTitle(),
            evaluation.getStatus());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar a JSON", e);
        }
    }

    private List<QuestionFeedback> fromJson(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<QuestionFeedback>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Feedback guardado con formato inválido", e);
        }
    }

    private static String newId() {
        return UUID.randomUUID().toString();
    }
}
