package com.sistema_notas.app.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sistema_notas.app.entidad.Enrollment;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {
    List<Enrollment> findByStudentId(String studentId);
    List<Enrollment> findByCourseId(String courseId);
}