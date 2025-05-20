package com.sistema_notas.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginUser) {
        if (loginUser == null || loginUser.getUserId() == null || loginUser.getPassword() == null) {
            return ResponseEntity.badRequest().body("ID o contraseña no pueden ser nulos");
        }

        User user = userRepository.findByUserId(loginUser.getUserId())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Contraseña incorrecta");
        }

        return ResponseEntity.ok("Login exitoso como " + user.getRole());
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createUser(@RequestBody User newUser) {
        if (newUser == null || newUser.getUserId() == null || newUser.getPassword() == null || newUser.getRole() == null || newUser.getFullName() == null) {
            return ResponseEntity.badRequest().body("Todos los campos son obligatorios");
        }

        if (userRepository.findByUserId(newUser.getUserId()).isPresent()) {
            return ResponseEntity.badRequest().body("El ID ya existe");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        userRepository.save(newUser);

        return ResponseEntity.ok("Usuario creado exitosamente con rol " + newUser.getRole());
    }
}