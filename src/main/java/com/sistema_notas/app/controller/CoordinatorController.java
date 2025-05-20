package com.sistema_notas.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sistema_notas.app.entidad.Course;
import com.sistema_notas.app.entidad.Student;
import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.CourseRepository;
import com.sistema_notas.app.repositorio.StudentRepository;
import com.sistema_notas.app.repositorio.UserRepository;

@Controller
public class CoordinatorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/coordinator")
    public String coordinatorPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        if (user == null) {
            return "error";
        }
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Coordinador");

        List<Student> students = studentRepository.findAll();
        Map<String, List<Student>> studentsByProgram = students.isEmpty() ? new HashMap<>() :
                students.stream().collect(Collectors.groupingBy(Student::getProgram));
        model.addAttribute("studentsByProgram", studentsByProgram);

        long matriculados = studentRepository.findByStatus("MATRICULADO").size();
        long activos = studentRepository.findByStatus("ACTIVO").size();
        long inactivos = studentRepository.findByStatus("INACTIVO").size();
        Map<String, Long> stats = new HashMap<>();
        stats.put("matriculados", matriculados);
        stats.put("activos", activos);
        stats.put("inactivos", inactivos);
        model.addAttribute("stats", stats);

        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses != null ? courses : List.of());

        List<User> teachers = userRepository.findByRole("TEACHER");
        model.addAttribute("teachers", teachers != null ? teachers : List.of());

        Map<String, String> teachersMap = teachers.stream()
                .collect(Collectors.toMap(User::getUserId, User::getFullName));
        model.addAttribute("teachersMap", teachersMap);

        return "coordinator";
    }

    @PostMapping("/coordinator/assign-teacher")
    public String assignTeacher(@RequestParam String courseId, @RequestParam String teacherId, Model model) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setProfessorId(teacherId);
            courseRepository.save(course);
        }
        return "redirect:/coordinator";
    }

    @GetMapping("/coordinator/create-course")
    public String createCoursePage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        if (user == null) {
            return "error";
        }
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Coordinador");
        return "create-course";
    }

    @PostMapping("/coordinator/create-course")
    public String createCourse(@RequestParam String name, @RequestParam String schedule, Model model) {
        Course course = new Course();
        course.setId(java.util.UUID.randomUUID().toString());
        course.setName(name);
        course.setSchedule(schedule);
        courseRepository.save(course);
        return "redirect:/coordinator";
    }

    @GetMapping("/coordinator/register-student")
    public String registerStudentPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        if (user == null) {
            return "error";
        }
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Coordinador");
        return "register-student";
    }

    @PostMapping("/coordinator/register-student")
    public String registerStudent(
            @RequestParam String studentId,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String program,
            @RequestParam String status,
            Model model) {
        if (studentRepository.findByStudentId(studentId).isPresent()) {
            model.addAttribute("error", "El ID del estudiante ya existe.");
            return "register-student";
        }

        Student student = new Student();
        student.setId(java.util.UUID.randomUUID().toString());
        student.setStudentId(studentId);
        student.setFullName(fullName);
        student.setPhone(phone);
        student.setProgram(program);
        student.setStatus(status);
        studentRepository.save(student);
        return "redirect:/coordinator";
    }

    @PostMapping("/coordinator/update-student-status")
    public String updateStudentStatus(@RequestParam String studentId, @RequestParam String status, Model model) {
        Student student = studentRepository.findByStudentId(studentId).orElse(null);
        if (student != null) {
            student.setStatus(status);
            studentRepository.save(student);
        }
        return "redirect:/coordinator";
    }
}