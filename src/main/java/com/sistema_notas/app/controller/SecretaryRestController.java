package com.sistema_notas.app.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_notas.app.entidad.ModificationLog;
import com.sistema_notas.app.entidad.Student;
import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.ModificationLogRepository;
import com.sistema_notas.app.repositorio.StudentRepository;
import com.sistema_notas.app.repositorio.UserRepository;

@RestController
@RequestMapping("/api/secretary")
public class SecretaryRestController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ModificationLogRepository modificationLogRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register-student")
    public ResponseEntity<String> registerStudent(@RequestBody Student student, Authentication authentication) {
        try {
            if (studentRepository.findByStudentId(student.getStudentId()).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El estudiante con ID " + student.getStudentId() + " ya existe.");
            }

            if (student.getFullName() == null || student.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El nombre completo del estudiante es obligatorio.");
            }

            studentRepository.save(student);

            ModificationLog log = new ModificationLog();
            log.setStudentId(student.getStudentId());
            log.setAction("Registro de estudiante: " + student.getFullName());
            log.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            modificationLogRepository.save(log);

            return ResponseEntity.ok("Estudiante registrado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar el estudiante: " + e.getMessage());
        }
    }

    @PostMapping("/update-contact")
    public ResponseEntity<String> updateContact(@RequestBody Student student, Authentication authentication) {
        try {
            Student existingStudent = studentRepository.findByStudentId(student.getStudentId()).orElse(null);
            if (existingStudent == null) {
                return ResponseEntity.badRequest().body("Error: El estudiante con ID " + student.getStudentId() + " no existe.");
            }

            existingStudent.setEmail(student.getEmail());
            existingStudent.setPhone(student.getPhone());
            studentRepository.save(existingStudent);

            ModificationLog log = new ModificationLog();
            log.setStudentId(student.getStudentId());
            log.setAction("Actualización de datos de contacto del estudiante: " + existingStudent.getFullName());
            log.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            modificationLogRepository.save(log);

            return ResponseEntity.ok("Datos de contacto actualizados exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar los datos de contacto: " + e.getMessage());
        }
    }
    
    @PostMapping("/update-phone")
    public ResponseEntity<String> updatePhone(@RequestBody Map<String, String> payload, Authentication authentication) {
        try {
            User user = userRepository.findByUserId(authentication.getName()).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("Error: Usuario no encontrado");
            }

            String phone = payload.get("phone");
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El número telefónico no puede estar vacío");
            }

            user.setPhone(phone);
            userRepository.save(user);

            Student student = studentRepository.findByStudentId(user.getUserId()).orElse(null);
            if (student != null) {
                student.setPhone(phone);
                studentRepository.save(student);
            }

            return ResponseEntity.ok("Número telefónico actualizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar el número telefónico: " + e.getMessage());
        }
    }

}