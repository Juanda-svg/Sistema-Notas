package com.sistema_notas.app.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sistema_notas.app.entidad.Course;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByProfessorId(String professorId);
}