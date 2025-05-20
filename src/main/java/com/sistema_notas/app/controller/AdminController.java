package com.sistema_notas.app.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    public String adminPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Administrador");
        model.addAttribute("users", userRepository.findAll()); 

        long totalStudents = studentRepository.count();
        long totalCourses = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("totalCourses", totalCourses);
        stats.put("totalEnrollments", totalEnrollments);
        model.addAttribute("stats", stats);

        return "admin";
    }

    @GetMapping("/admin/create-user")
    public String createUserPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Administrador");
        return "create-user";
    }
}