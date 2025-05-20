package com.sistema_notas.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_notas.app.entidad.User;
import com.sistema_notas.app.repositorio.UserRepository;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(
            @RequestParam String userId,
            @RequestParam String fullName,
            @RequestParam(required = false) String email, // Hacer email opcional
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String role,
            Authentication authentication) {
        try {
            if (userRepository.findByUserId(userId).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El usuario con ID " + userId + " ya existe.");
            }
            if (email != null && !email.isEmpty() && userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El correo " + email + " ya está en uso.");
            }

            User user = new User();
            user.setUserId(userId);
            user.setFullName(fullName);
            user.setEmail(email); // Si email es null, se establecerá como null
            user.setPhone(phone);
            user.setPassword(password);
            user.setRole(role);
            userRepository.save(user);

            return ResponseEntity.ok("Usuario creado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el usuario: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(Authentication authentication) {
        try {
            return ResponseEntity.ok(userRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/search-users")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query, Authentication authentication) {
        try {
            return ResponseEntity.ok(userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/update-user/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable String userId,
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            @RequestParam String phone,
            @RequestParam String role,
            Authentication authentication) {
        try {
            if (!userRepository.findByUserId(userId).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El usuario con ID " + userId + " no existe.");
            }
            User existingUser = userRepository.findByUserId(userId).get();
            if (email != null && !email.isEmpty() && !existingUser.getEmail().equals(email) && userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El correo " + email + " ya está en uso.");
            }

            existingUser.setFullName(fullName);
            if (email != null && !email.isEmpty()) {
                existingUser.setEmail(email);
            }
            existingUser.setPhone(phone);
            existingUser.setRole(role);
            userRepository.save(existingUser);

            return ResponseEntity.ok("Usuario actualizado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId, Authentication authentication) {
        try {
            if (!userRepository.findByUserId(userId).isPresent()) {
                return ResponseEntity.badRequest().body("Error: El usuario con ID " + userId + " no existe.");
            }
            userRepository.deleteByUserId(userId);
            return ResponseEntity.ok("Usuario eliminado exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar el usuario: " + e.getMessage());
        }
    }
}