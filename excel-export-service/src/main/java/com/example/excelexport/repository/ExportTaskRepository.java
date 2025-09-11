package com.example.excelexport.repository;

import com.example.excelexport.entity.ExportTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExportTaskRepository extends JpaRepository<ExportTask, Long> {
    Optional<ExportTask> findByTaskId(String taskId);
}
