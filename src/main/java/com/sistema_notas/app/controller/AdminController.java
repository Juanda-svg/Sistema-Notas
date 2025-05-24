package com.sistema_notas.app.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.CourseRepository;
import com.sistema_notas.app.repositorio.EnrollmentRepository;
import com.sistema_notas.app.repositorio.StudentRepository;
import com.sistema_notas.app.repositorio.UserRepository;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @GetMapping("/admin")
    public String adminPage(Model model, Authentication authentication, @RequestParam(required = false) String roleFilter) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user != null ? user.getFullName() : "Administrador");
        model.addAttribute("role", "Administrador");

        // Estadísticas generales
        long totalStudents = studentRepository.count();
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        Map<String, Object> generalStats = new HashMap<>();
        generalStats.put("totalStudents", totalStudents);
        generalStats.put("totalCourses", totalCourses);
        generalStats.put("totalEnrollments", totalEnrollments);
        model.addAttribute("generalStats", generalStats);

        // Estadísticas por rol
        Map<String, Long> roleCounts = new HashMap<>();
        roleCounts.put("ADMIN", userRepository.countByRole("ADMIN"));
        roleCounts.put("COORDINATOR", userRepository.countByRole("COORDINATOR"));
        roleCounts.put("TEACHER", userRepository.countByRole("TEACHER"));
        roleCounts.put("SECRETARY", userRepository.countByRole("SECRETARY"));
        roleCounts.put("STUDENT", userRepository.countByRole("STUDENT"));
        model.addAttribute("roleCounts", roleCounts);

        // Filtro por rol
        if (roleFilter != null && !roleFilter.isEmpty()) {
            model.addAttribute("users", userRepository.findByRole(roleFilter));
        } else {
            model.addAttribute("users", userRepository.findAll());
        }
        model.addAttribute("roleFilter", roleFilter);

        return "admin";
    }

    @GetMapping("/admin/create-user")
    public String createUserPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user != null ? user.getFullName() : "Administrador");
        model.addAttribute("role", "Administrador");
        return "create-user";
    }
}