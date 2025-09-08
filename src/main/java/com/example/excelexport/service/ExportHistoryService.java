package com.example.excelexport.service;

import com.example.excelexport.entity.ExportHistory;
import com.example.excelexport.repository.ExportHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExportHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportHistoryService.class);
    
    @Autowired
    private ExportHistoryRepository exportHistoryRepository;
    
    @Autowired
    private MinioService minioService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public ExportHistory createExportHistory(String requestId, String exportType, Map<String, Object> paramFilters) {
        try {
            String paramFiltersJson = objectMapper.writeValueAsString(paramFilters);
            ExportHistory history = new ExportHistory(requestId, exportType, paramFiltersJson);
            return exportHistoryRepository.save(history);
        } catch (Exception e) {
            logger.error("Failed to create export history for request: {}", requestId, e);
            throw new RuntimeException("Failed to create export history", e);
        }
    }
    
    public void updateStatus(String requestId, ExportHistory.ExportStatus status) {
        Optional<ExportHistory> historyOpt = exportHistoryRepository.findByRequestId(requestId);
        if (historyOpt.isPresent()) {
            ExportHistory history = historyOpt.get();
            history.setStatus(status);
            if (status == ExportHistory.ExportStatus.COMPLETED) {
                history.setCompletedTime(LocalDateTime.now());
            }
            exportHistoryRepository.save(history);
        }
    }
    
    public void updateWithFileInfo(String requestId, String filePath, String fileName, long fileSize) {
        Optional<ExportHistory> historyOpt = exportHistoryRepository.findByRequestId(requestId);
        if (historyOpt.isPresent()) {
            ExportHistory history = historyOpt.get();
            history.setFilePath(filePath);
            history.setFileName(fileName);
            history.setFileSize(fileSize);
            history.setStatus(ExportHistory.ExportStatus.COMPLETED);
            history.setCompletedTime(LocalDateTime.now());
            exportHistoryRepository.save(history);
        }
    }
    
    public void updateWithError(String requestId, String errorMessage) {
        Optional<ExportHistory> historyOpt = exportHistoryRepository.findByRequestId(requestId);
        if (historyOpt.isPresent()) {
            ExportHistory history = historyOpt.get();
            history.setStatus(ExportHistory.ExportStatus.FAILED);
            history.setErrorMessage(errorMessage);
            history.setCompletedTime(LocalDateTime.now());
            exportHistoryRepository.save(history);
        }
    }
    
    public Optional<ExportHistory> findByRequestId(String requestId) {
        return exportHistoryRepository.findByRequestId(requestId);
    }
    
    public List<ExportHistory> findByExportType(String exportType) {
        return exportHistoryRepository.findByExportTypeOrderByTimeRequestDesc(exportType);
    }
    
    public List<ExportHistory> findByStatus(ExportHistory.ExportStatus status) {
        return exportHistoryRepository.findByStatusOrderByTimeRequestDesc(status);
    }
    
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredExports() {
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(7);
        List<ExportHistory> expiredExports = exportHistoryRepository.findExpiredExports(expiredTime);
        
        for (ExportHistory export : expiredExports) {
            try {
                if (export.getFilePath() != null && minioService.fileExists(export.getFilePath())) {
                    minioService.deleteFile(export.getFilePath());
                }
                export.setStatus(ExportHistory.ExportStatus.EXPIRED);
                exportHistoryRepository.save(export);
                logger.info("Expired export: {}", export.getRequestId());
            } catch (Exception e) {
                logger.error("Failed to cleanup expired export: {}", export.getRequestId(), e);
            }
        }
    }
    
    @Scheduled(fixedRate = 1800000)
    public void handleStuckProcessingExports() {
        LocalDateTime stuckTime = LocalDateTime.now().minusHours(2);
        List<ExportHistory> stuckExports = exportHistoryRepository.findStuckProcessingExports(stuckTime);
        
        for (ExportHistory export : stuckExports) {
            export.setStatus(ExportHistory.ExportStatus.FAILED);
            export.setErrorMessage("Export processing timeout");
            export.setCompletedTime(LocalDateTime.now());
            exportHistoryRepository.save(export);
            logger.warn("Marked stuck export as failed: {}", export.getRequestId());
        }
    }
}
