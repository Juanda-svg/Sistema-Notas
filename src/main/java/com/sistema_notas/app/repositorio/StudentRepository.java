package com.sistema_notas.app.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sistema_notas.app.entidad.Student;

public interface StudentRepository extends MongoRepository<Student, String> {
    List<Student> findByProgram(String program);
    List<Student> findByStatus(String status);
    Optional<Student> findByStudentId(String studentId);
}