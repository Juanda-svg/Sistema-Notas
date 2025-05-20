package com.sistema_notas.app.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import com.sistema_notas.app.entidad.ModificationLog;
import com.sistema_notas.app.entidad.Student;
import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.ModificationLogRepository;
import com.sistema_notas.app.repositorio.StudentRepository;
import com.sistema_notas.app.repositorio.UserRepository;

@Controller
public class SecretaryController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ModificationLogRepository modificationLogRepository;

    @GetMapping("/secretary")
    public String secretaryPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Secretaria");

        List<Student> students = studentRepository.findAll();
        model.addAttribute("students", students);

        List<ModificationLog> modificationLogs = modificationLogRepository.findAll();
        model.addAttribute("modificationLogs", modificationLogs);

        return "secretary";
    }

    @GetMapping("/secretary/register-student")
    public String registerStudentPage(Model model, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Secretaria");
        return "register-student";
    }

    @GetMapping("/secretary/edit-contact/{studentId}")
    public String editContactPage(Model model, @PathVariable String studentId, Authentication authentication) {
        User user = userRepository.findByUserId(authentication.getName()).orElse(null);
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("role", "Secretaria");

        Student student = studentRepository.findByStudentId(studentId).orElse(null);
        if (student == null) {
            model.addAttribute("error", "El estudiante con ID " + studentId + " no fue encontrado.");
            return "error";
        }
        model.addAttribute("student", student);
        return "edit-contact";
    }
}