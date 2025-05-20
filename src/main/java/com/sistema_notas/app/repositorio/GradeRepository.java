package com.sistema_notas.app.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sistema_notas.app.entidad.Grade;

public interface GradeRepository extends MongoRepository<Grade, String> {
    List<Grade> findByCourseId(String courseId);
    List<Grade> findByStudentId(String studentId);
    Optional<Grade> findByStudentIdAndCourseId(String studentId, String courseId);
}