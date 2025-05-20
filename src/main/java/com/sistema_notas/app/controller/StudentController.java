package com.sistema_notas.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



import com.sistema_notas.app.entidad.Course;
import com.sistema_notas.app.entidad.Enrollment;
import com.sistema_notas.app.entidad.Grade;
import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.CourseRepository;
import com.sistema_notas.app.repositorio.EnrollmentRepository;
import com.sistema_notas.app.repositorio.GradeRepository;
import com.sistema_notas.app.repositorio.UserRepository;





@Controller
public class StudentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @GetMapping("/student")
    public String studentPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Estudiante");
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("phone", user.getPhone() != null ? user.getPhone() : "");

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(user.getUserId());
        List<Map<String, String>> courses = enrollments.stream()
                .map(e -> {
                    Course course = courseRepository.findById(e.getCourseId()).orElse(null);
                    Map<String, String> courseData = new HashMap<>();
                    courseData.put("name", course != null ? course.getName() : "Desconocido");
                    courseData.put("schedule", course != null ? course.getSchedule() : "N/A");
                    return courseData;
                })
                .collect(Collectors.toList());
        model.addAttribute("courses", courses);

        // Añadir calificaciones desde GradeRepository
        List<Map<String, Object>> gradesSummary = enrollments.stream()
                .map(e -> {
                    Course course = courseRepository.findById(e.getCourseId()).orElse(null);
                    Optional<Grade> gradeOptional = gradeRepository.findByStudentIdAndCourseId(user.getUserId(), e.getCourseId());
                    Grade grade = gradeOptional.orElse(null);
                    Map<String, Object> gradeData = new HashMap<>();
                    gradeData.put("courseName", course != null ? course.getName() : "Desconocido");
                    gradeData.put("grade", grade != null && grade.getScore() != null ? grade.getScore() : "N/A");

                    // Determinar el estado de la materia
                    String status = "Aprobado"; // Estado por defecto
                    if (grade != null && grade.getScore() != null) {
                        try {
                            double score = grade.getScore();
                            status = score < 3.0 ? "Reprobado" : "Aprobado";
                        } catch (NumberFormatException ex) {
                            status = "Error en la calificación";
                        }
                    }
                    gradeData.put("status", status);

                    return gradeData;
                })
                .collect(Collectors.toList());
        model.addAttribute("gradesSummary", gradesSummary);

        // Alerta de materias reprobadas
        boolean hasFailedCourses = gradesSummary.stream()
                .anyMatch(grade -> {
                    Object gradeValue = grade.get("grade");
                    return gradeValue instanceof Double && (Double) gradeValue < 3.0;
                });
        model.addAttribute("hasFailedCourses", hasFailedCourses);

        List<Course> availableCourses = courseRepository.findAll();
        model.addAttribute("availableCourses", availableCourses);

        return "student";
    }
}