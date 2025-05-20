package com.sistema_notas.app.config;

import com.sistema_notas.app.entidad.*;
import com.sistema_notas.app.repositorio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ModificationLogRepository modificationLogRepository;

    // No necesitamos PasswordEncoder porque estamos usando NoOpPasswordEncoder
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar Usuarios
        if (userRepository.findByUserId("123456789").isEmpty()) {
            User admin = new User();
            admin.setUserId("123456789");
            admin.setPassword("admin123"); // Guardar en texto plano
            admin.setRole("ADMIN");
            admin.setFullName("Administrador Principal");
            userRepository.save(admin);
            System.out.println("Usuario ADMIN creado: 123456789");
        }
        if (userRepository.findByUserId("987654321").isEmpty()) {
            User coordinator = new User();
            coordinator.setUserId("987654321");
            coordinator.setPassword("coord123"); // Guardar en texto plano
            coordinator.setRole("COORDINATOR");
            coordinator.setFullName("Coordinador Uno");
            userRepository.save(coordinator);
            System.out.println("Usuario COORDINATOR creado: 987654321");
        }
        if (userRepository.findByUserId("456789123").isEmpty()) {
            User teacher = new User();
            teacher.setUserId("456789123");
            teacher.setPassword("teacher123"); // Guardar en texto plano
            teacher.setRole("TEACHER");
            teacher.setFullName("Profesor Uno");
            userRepository.save(teacher);
            System.out.println("Usuario TEACHER creado: 456789123");
        }
        if (userRepository.findByUserId("321654987").isEmpty()) {
            User secretary = new User();
            secretary.setUserId("321654987");
            secretary.setPassword("secretary123"); // Guardar en texto plano
            secretary.setRole("SECRETARY");
            secretary.setFullName("Secretaria Uno");
            userRepository.save(secretary);
            System.out.println("Usuario SECRETARY creado: 321654987");
        }
        if (userRepository.findByUserId("654987321").isEmpty()) {
            User student = new User();
            student.setUserId("654987321");
            student.setPassword("student123"); // Guardar en texto plano
            student.setRole("STUDENT");
            student.setFullName("Estudiante Uno");
            userRepository.save(student);
            System.out.println("Usuario STUDENT creado: 654987321");
        }

        // Obtener el _id del profesor
        Optional<User> teacherOpt = userRepository.findByUserId("456789123");
        if (!teacherOpt.isPresent()) {
            throw new RuntimeException("No se encontró el profesor con userId 456789123");
        }
        String teacherId = teacherOpt.get().getId();

        // Inicializar Cursos
        if (courseRepository.findAll().isEmpty()) {
            Course c1 = new Course();
            c1.setCourseCode("CS101");
            c1.setName("Programación I");
            c1.setProfessorId(teacherId); // Usar el _id del usuario
            c1.setSchedule("Lunes 8:00-10:00");
            courseRepository.save(c1);
            System.out.println("Curso creado: Programación I");

            Course c2 = new Course();
            c2.setCourseCode("CS102");
            c2.setName("Programación II");
            c2.setProfessorId(teacherId); // Usar el _id del usuario
            c2.setSchedule("Miércoles 10:00-12:00");
            courseRepository.save(c2);
            System.out.println("Curso creado: Programación II");
        }

        // Inicializar Estudiantes
        if (studentRepository.findAll().isEmpty()) {
            Student s1 = new Student();
            s1.setStudentId("S001");
            s1.setFullName("Juan Pérez");
            s1.setProgram("Ingeniería de Sistemas");
            s1.setStatus("MATRICULADO");
            s1.setEmail("juan.perez@uni.edu");
            studentRepository.save(s1);
            System.out.println("Estudiante creado: Juan Pérez");

            Student s2 = new Student();
            s2.setStudentId("S002");
            s2.setFullName("María Gómez");
            s2.setProgram("Ingeniería de Sistemas");
            s2.setStatus("ACTIVO");
            s2.setEmail("maria.gomez@uni.edu");
            studentRepository.save(s2);
            System.out.println("Estudiante creado: María Gómez");

            Student s3 = new Student();
            s3.setStudentId("S003");
            s3.setFullName("Carlos López");
            s3.setProgram("Administración");
            s3.setStatus("INACTIVO");
            s3.setEmail("carlos.lopez@uni.edu");
            studentRepository.save(s3);
            System.out.println("Estudiante creado: Carlos López");
        }

        // Inicializar Inscripciones
        if (enrollmentRepository.findAll().isEmpty()) {
            Enrollment e1 = new Enrollment();
            e1.setStudentId(studentRepository.findAll().get(0).getId());
            e1.setCourseId(courseRepository.findAll().get(0).getId());
            enrollmentRepository.save(e1);
            System.out.println("Inscripción creada para estudiante 1, curso 1");

            Enrollment e2 = new Enrollment();
            e2.setStudentId(studentRepository.findAll().get(1).getId());
            e2.setCourseId(courseRepository.findAll().get(0).getId());
            enrollmentRepository.save(e2);
            System.out.println("Inscripción creada para estudiante 2, curso 1");

            Enrollment e3 = new Enrollment();
            e3.setStudentId(studentRepository.findAll().get(0).getId());
            e3.setCourseId(courseRepository.findAll().get(1).getId());
            enrollmentRepository.save(e3);
            System.out.println("Inscripción creada para estudiante 1, curso 2");
        }

        // Inicializar Calificaciones
        if (gradeRepository.findAll().isEmpty()) {
            Grade g1 = new Grade();
            g1.setStudentId(studentRepository.findAll().get(0).getId());
            g1.setCourseId(courseRepository.findAll().get(0).getId());
            g1.setScore(4.5);
            gradeRepository.save(g1);
            System.out.println("Calificación creada para estudiante 1, curso 1");

            Grade g2 = new Grade();
            g2.setStudentId(studentRepository.findAll().get(1).getId());
            g2.setCourseId(courseRepository.findAll().get(0).getId());
            g2.setScore(2.8);
            gradeRepository.save(g2);
            System.out.println("Calificación creada para estudiante 2, curso 1");
        }

        // Inicializar Logs de Modificaciones
        if (modificationLogRepository.findAll().isEmpty()) {
            ModificationLog log = new ModificationLog();
            log.setStudentId(studentRepository.findAll().get(0).getId());
            log.setAction("Registro de estudiante");
            log.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            modificationLogRepository.save(log);
            System.out.println("Log de modificación creado");
        }
    }
}