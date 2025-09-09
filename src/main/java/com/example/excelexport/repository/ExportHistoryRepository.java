package com.example.excelexport.repository;

import com.example.excelexport.entity.ExportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExportHistoryRepository extends JpaRepository<ExportHistory, Long> {
    
    Optional<ExportHistory> findByRequestId(String requestId);
    
    List<ExportHistory> findByExportTypeOrderByTimeRequestDesc(String exportType);
    
    List<ExportHistory> findByStatusOrderByTimeRequestDesc(ExportHistory.ExportStatus status);
    
    @Query("SELECT e FROM ExportHistory e WHERE e.timeRequest < :expiredTime AND e.status = 'COMPLETED'")
    List<ExportHistory> findExpiredExports(LocalDateTime expiredTime);
    
    @Query("SELECT e FROM ExportHistory e WHERE e.status = 'PROCESSING' AND e.timeRequest < :stuckTime")
    List<ExportHistory> findStuckProcessingExports(LocalDateTime stuckTime);
}

