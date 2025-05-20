package com.sistema_notas.app.repositorio;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sistema_notas.app.entidad.ModificationLog;

public interface ModificationLogRepository extends MongoRepository<ModificationLog, String> {
    List<ModificationLog> findByStudentId(String studentId);
}