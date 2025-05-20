package com.sistema_notas.app.controller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sistema_notas.app.entidad.Course;
import com.sistema_notas.app.entidad.Enrollment;
import com.sistema_notas.app.entidad.Grade;
import com.sistema_notas.app.entidad.Student;
import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.CourseRepository;
import com.sistema_notas.app.repositorio.EnrollmentRepository;
import com.sistema_notas.app.repositorio.GradeRepository;
import com.sistema_notas.app.repositorio.StudentRepository;
import com.sistema_notas.app.repositorio.UserRepository;

@Controller
public class TeacherController {

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

    @GetMapping("/teacher")
    public String teacherPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Profesor");

        List<Course> courses = courseRepository.findByProfessorId(authentication.getName());
        model.addAttribute("courses", courses);

        Map<String, List<Map<String, Object>>> studentsByCourse = new HashMap<>();
        for (Course course : courses) {
            List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
            List<Map<String, Object>> studentDataList = enrollments.stream()
                    .map(e -> {
                        Map<String, Object> studentData = new HashMap<>();
                        Student student = studentRepository.findById(e.getStudentId()).orElse(null);
                        studentData.put("studentName", student != null ? student.getFullName() : "Desconocido");
                        Grade grade = gradeRepository.findByCourseId(course.getId()).stream()
                                .filter(g -> g.getStudentId().equals(e.getStudentId()))
                                .findFirst().orElse(null);
                        studentData.put("score", grade != null ? grade.getScore() : null);
                        String performance = "Sin calificación";
                        if (grade != null) {
                            double score = grade.getScore();
                            if (score < 3.0) {
                                performance = "Bajo";
                            } else if (score < 4.0) {
                                performance = "Medio";
                            } else {
                                performance = "Alto";
                            }
                        }
                        studentData.put("performance", performance);
                        return studentData;
                    })
                    .sorted(Comparator.comparing((Map<String, Object> m) -> {
                        Double score = (Double) m.get("score");
                        return score != null ? score : Double.MAX_VALUE;
                    }))
                    .collect(Collectors.toList());
            studentsByCourse.put(course.getName(), studentDataList);
        }
        model.addAttribute("studentsByCourse", studentsByCourse);

        return "teacher";
    }

    @GetMapping("/teacher/enter-grades/{courseId}")
    public String enterGradesPage(Model model, @PathVariable String courseId, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Profesor");

        Course course = courseRepository.findById(courseId).orElse(null);
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Map<String, Object>> students = enrollments.stream()
                .map(e -> {
                    Map<String, Object> studentData = new HashMap<>();
                    Student student = studentRepository.findById(e.getStudentId()).orElse(null);
                    studentData.put("studentId", e.getStudentId());
                    studentData.put("studentName", student != null ? student.getFullName() : "Desconocido");
                    Grade grade = gradeRepository.findByCourseId(courseId).stream()
                            .filter(g -> g.getStudentId().equals(e.getStudentId()))
                            .findFirst().orElse(null);
                    studentData.put("currentGrade", grade != null ? grade.getScore() : null);
                    return studentData;
                })
                .collect(Collectors.toList());
        model.addAttribute("course", course);
        model.addAttribute("students", students);

        return "enter-grades";
    }

    @PostMapping("/teacher/save-grades")
    public String saveGrades(@RequestParam String courseId, @RequestParam Map<String, String> grades, Model model) {
        for (Map.Entry<String, String> entry : grades.entrySet()) {
            if (entry.getKey().startsWith("grade_")) {
                String studentId = entry.getKey().replace("grade_", "");
                double score = Double.parseDouble(entry.getValue());
                List<Grade> existingGrades = gradeRepository.findByCourseId(courseId).stream()
                        .filter(g -> g.getStudentId().equals(studentId))
                        .collect(Collectors.toList());
                if (!existingGrades.isEmpty()) {
                    Grade grade = existingGrades.get(0);
                    grade.setScore(score);
                    gradeRepository.save(grade);
                } else {
                    Grade grade = new Grade();
                    grade.setStudentId(studentId);
                    grade.setCourseId(courseId);
                    grade.setScore(score);
                    gradeRepository.save(grade);
                }
            }
        }
        return "redirect:/teacher/enter-grades/" + courseId + "?success=true";
    }
}