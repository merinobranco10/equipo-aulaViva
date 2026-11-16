package cl.aulaviva.demo;

import cl.aulaviva.academic.AcademicDirectory;
import cl.aulaviva.academic.Course;
import cl.aulaviva.academic.CourseRepository;
import cl.aulaviva.evaluations.EvaluationService;
import cl.aulaviva.evaluations.dto.AnswerInput;
import cl.aulaviva.evaluations.dto.CreateEvaluationRequest;
import cl.aulaviva.evaluations.dto.EvaluationCreatedResponse;
import cl.aulaviva.evaluations.dto.EvaluationResponse;
import cl.aulaviva.evaluations.dto.QuestionInput;
import cl.aulaviva.evaluations.dto.SubmissionRequest;
import cl.aulaviva.identity.AppUser;
import cl.aulaviva.identity.AppUserRepository;
import cl.aulaviva.identity.AulaVivaUser;
import cl.aulaviva.identity.Role;
import cl.aulaviva.tenancy.Tenant;
import cl.aulaviva.tenancy.TenantRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Datos de demo (perfil "demo"): 2 colegios, 6 usuarios y 3 evaluaciones con preguntas del
 * temario. Todos los nombres son ficticios. La contrasena llega por DEMO_PASSWORD y se guarda
 * con BCrypt. Si ya hay datos (por ejemplo, PostgreSQL con volumen), no hace nada.
 */
@Component
@Profile("demo")
public class DemoDataLoader implements CommandLineRunner {

    public static final String TENANT_A = "colegio-a";
    public static final String TENANT_B = "colegio-b";
    public static final String COURSE_A = "matematica-8a";
    public static final String COURSE_B = "ciencias-8b";
    public static final String TEACHER_A = "u-docente-a";
    public static final String STUDENT_A = "u-estudiante-a";
    public static final String GUARDIAN_A = "u-apoderado-a";
    public static final String TEACHER_B = "u-docente-b";
    public static final String STUDENT_B = "u-estudiante-b";
    public static final String COORDINATOR_B = "u-coordinador-b";

    private static final Logger LOG = LoggerFactory.getLogger(DemoDataLoader.class);

    private final TenantRepository tenants;
    private final AppUserRepository users;
    private final CourseRepository courses;
    private final AcademicDirectory directory;
    private final EvaluationService evaluations;
    private final PasswordEncoder encoder;
    private final String demoPassword;

    public DemoDataLoader(TenantRepository tenants, AppUserRepository users,
            CourseRepository courses, AcademicDirectory directory, EvaluationService evaluations,
            PasswordEncoder encoder, @Value("${aulaviva.demo.password:}") String demoPassword) {
        this.tenants = tenants;
        this.users = users;
        this.courses = courses;
        this.directory = directory;
        this.evaluations = evaluations;
        this.encoder = encoder;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(String... args) {
        if (tenants.count() > 0) {
            LOG.info("Datos de demo ya cargados; no se vuelven a crear.");
            return;
        }
        if (demoPassword == null || demoPassword.isBlank()) {
            throw new IllegalStateException(
                "Perfil demo activo sin DEMO_PASSWORD. Define la variable de entorno DEMO_PASSWORD.");
        }
        String hash = encoder.encode(demoPassword);

        tenants.save(new Tenant(TENANT_A, "Colegio Los Andes (demo)"));
        tenants.save(new Tenant(TENANT_B, "Colegio Pacífico (demo)"));

        AppUser teacherA = users.save(new AppUser(TEACHER_A, TENANT_A, "docente.a", hash,
            "Camila Rojas", Role.TEACHER));
        AppUser studentA = users.save(new AppUser(STUDENT_A, TENANT_A, "estudiante.a", hash,
            "Tomás Fuentes", Role.STUDENT));
        users.save(new AppUser(GUARDIAN_A, TENANT_A, "apoderado.a", hash, "Paula Fuentes",
            Role.GUARDIAN));
        AppUser teacherB = users.save(new AppUser(TEACHER_B, TENANT_B, "docente.b", hash,
            "Andrés Soto", Role.TEACHER));
        users.save(new AppUser(STUDENT_B, TENANT_B, "estudiante.b", hash, "Martina Vera",
            Role.STUDENT));
        users.save(new AppUser(COORDINATOR_B, TENANT_B, "coordinador.b", hash,
            "Rodrigo Pizarro", Role.COORDINATOR));

        courses.save(new Course(COURSE_A, TENANT_A, "Matemática 8° básico A", TEACHER_A));
        courses.save(new Course(COURSE_B, TENANT_B, "Ciencias Naturales 8° básico B", TEACHER_B));
        directory.enroll(COURSE_A, STUDENT_A);
        directory.enroll(COURSE_B, STUDENT_B);
        directory.linkGuardian(GUARDIAN_A, STUDENT_A);

        AulaVivaUser docenteA = AulaVivaUser.from(teacherA);
        EvaluationCreatedResponse enteros = evaluations.create(docenteA,
            new CreateEvaluationRequest(COURSE_A, "Números enteros: multiplicación y división",
                List.of(
                    question("¿Cuál es el resultado de (-3) · (-4)?",
                        List.of("-12", "12", "-7", "7"), 1,
                        "Al multiplicar dos enteros con el mismo signo, el resultado es positivo."),
                    question("¿Cuál es el resultado de (-6) : (-2)?",
                        List.of("-3", "3", "-12", "12"), 1,
                        "Al dividir dos enteros con el mismo signo, el cociente es positivo."),
                    question("¿Qué signo tiene el producto (-2) · 5 · (-1) · (-3)?",
                        List.of("Positivo", "Negativo", "Depende del orden", "Es cero"), 1,
                        "Hay tres factores negativos: una cantidad impar de signos negativos "
                            + "da un resultado negativo."))),
            "seed-mat-enteros");
        evaluations.create(docenteA,
            new CreateEvaluationRequest(COURSE_A, "Potencias de base entera", List.of(
                question("¿Cuál es el valor de (-2)³?", List.of("-8", "8", "-6", "6"), 0,
                    "Base negativa con exponente impar: el resultado es negativo."),
                question("¿Cuál es el valor de (-3)²?", List.of("-9", "9", "-6", "6"), 1,
                    "Base negativa con exponente par: el resultado es positivo."),
                question("¿Cuánto es 5⁰?", List.of("0", "1", "5", "No existe"), 1,
                    "Toda potencia de base distinta de cero y exponente cero es igual a 1."))),
            "seed-mat-potencias");
        evaluations.create(AulaVivaUser.from(teacherB),
            new CreateEvaluationRequest(COURSE_B, "La célula", List.of(
                question("¿Qué organelo realiza la fotosíntesis?",
                    List.of("Mitocondria", "Cloroplasto", "Ribosoma", "Núcleo"), 1,
                    "Los cloroplastos captan la energía de la luz en las células vegetales."),
                question("¿Qué estructura contiene el material genético en una célula eucarionte?",
                    List.of("Núcleo", "Pared celular", "Vacuola", "Membrana plasmática"), 0,
                    "En las células eucariontes el ADN está dentro del núcleo."),
                question("¿Qué estructura tienen las células vegetales y no las animales?",
                    List.of("Membrana plasmática", "Mitocondria", "Pared celular", "Citoplasma"),
                    2, "La pared celular da rigidez a las células vegetales."))),
            "seed-cie-celula");

        // estudiante.a ya rindio "Numeros enteros" (2 de 3 correctas) y tiene "Potencias"
        // pendiente: asi el panel del apoderado muestra datos desde el inicio.
        AulaVivaUser estudianteA = AulaVivaUser.from(studentA);
        EvaluationResponse rendida = evaluations.get(estudianteA, enteros.id());
        int[] choices = {1, 1, 0};
        List<AnswerInput> answers = new ArrayList<>();
        for (int i = 0; i < rendida.questions().size(); i++) {
            answers.add(new AnswerInput(rendida.questions().get(i).id(), choices[i]));
        }
        evaluations.submit(estudianteA, enteros.id(), new SubmissionRequest(answers),
            "seed-sub-enteros");

        LOG.info("Datos de demo cargados: 2 colegios, 6 usuarios, 3 evaluaciones.");
    }

    private static QuestionInput question(String text, List<String> options, int correct,
            String explanation) {
        return new QuestionInput(text, options, correct, 2, explanation);
    }
}
