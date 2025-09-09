package com.example.excelexport.controller;

import com.example.excelexport.entity.ExportHistory;
import com.example.excelexport.service.ExportHistoryService;
import com.example.excelexport.service.MinioService;
import com.example.excelexport.service.ExportFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);
    
    @Autowired
    private ExportHistoryService exportHistoryService;
    
    @Autowired
    private MinioService minioService;
    
    @Autowired
    private ExportFileService exportFileService;
    
    @GetMapping("/history")
    public ResponseEntity<List<ExportHistory>> getExportHistory(
            @RequestParam(required = false) String exportType,
            @RequestParam(required = false) String status) {
        
        List<ExportHistory> history;
        
        if (exportType != null) {
            history = exportHistoryService.findByExportType(exportType);
        } else if (status != null) {
            ExportHistory.ExportStatus exportStatus = ExportHistory.ExportStatus.valueOf(status.toUpperCase());
            history = exportHistoryService.findByStatus(exportStatus);
        } else {
            history = exportHistoryService.findByStatus(null);
        }
        
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/history/{requestId}")
    public ResponseEntity<ExportHistory> getExportById(@PathVariable String requestId) {
        Optional<ExportHistory> history = exportHistoryService.findByRequestId(requestId);
        return history.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/download/{requestId}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String requestId) {
        try {
            Optional<ExportHistory> historyOpt = exportHistoryService.findByRequestId(requestId);
            
            if (historyOpt.isEmpty()) {
                logger.warn("Export history not found for request: {}", requestId);
                return ResponseEntity.notFound().build();
            }
            
            ExportHistory history = historyOpt.get();
            
            if (history.getStatus() != ExportHistory.ExportStatus.COMPLETED) {
                logger.warn("Export not completed for request: {}", requestId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            if (history.getFilePath() == null) {
                logger.warn("File path not found for request: {}", requestId);
                return ResponseEntity.notFound().build();
            }
            
            if (!minioService.fileExists(history.getFilePath())) {
                logger.warn("File not found in MinIO for request: {}", requestId);
                return ResponseEntity.notFound().build();
            }
            
            InputStream fileStream = minioService.downloadFile(history.getFilePath());
            InputStreamResource resource = new InputStreamResource(fileStream);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + history.getFileName() + "\"");
            
            String contentType = history.getFileName().endsWith(".json") ? 
                "application/json" : 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            
            logger.info("File download initiated for request: {}", requestId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("Failed to download file for request: {}", requestId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/download-url/{requestId}")
    public ResponseEntity<String> getDownloadUrl(@PathVariable String requestId,
                                                @RequestParam(defaultValue = "3600") int expiryInSeconds) {
        try {
            Optional<ExportHistory> historyOpt = exportHistoryService.findByRequestId(requestId);
            
            if (historyOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ExportHistory history = historyOpt.get();
            
            if (history.getStatus() != ExportHistory.ExportStatus.COMPLETED || 
                history.getFilePath() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            String presignedUrl = minioService.getFileUrl(history.getFilePath(), expiryInSeconds);
            
            return ResponseEntity.ok(presignedUrl);
            
        } catch (Exception e) {
            logger.error("Failed to generate download URL for request: {}", requestId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/upload-json")
    public ResponseEntity<Map<String, String>> uploadJsonData(
            @RequestParam String exportType,
            @RequestBody String jsonData) {
        try {
            String requestId = UUID.randomUUID().toString();
            String filePath = exportFileService.uploadJsonData(requestId, exportType, jsonData);
            
            logger.info("Successfully uploaded JSON data for type: {} with request ID: {}", exportType, requestId);
            
            return ResponseEntity.ok(Map.of(
                "requestId", requestId,
                "filePath", filePath,
                "message", "JSON data uploaded successfully"
            ));
            
        } catch (Exception e) {
            logger.error("Failed to upload JSON data for export type: {}", exportType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload JSON data: " + e.getMessage()));
        }
    }
    
    @PostMapping("/upload-json-with-history")
    public ResponseEntity<Map<String, String>> uploadJsonDataWithHistory(
            @RequestParam String exportType,
            @RequestBody String jsonData) {
        try {
            String requestId = UUID.randomUUID().toString();
            
            ExportHistory history = exportHistoryService.createExportHistory(
                requestId, 
                exportType, 
                Map.of("manual_upload", true)
            );
            
            exportHistoryService.updateStatus(requestId, ExportHistory.ExportStatus.PROCESSING);
            
            String filePath = exportFileService.uploadJsonData(requestId, exportType, jsonData);
            String fileName = extractFileName(filePath);
            
            exportHistoryService.updateWithFileInfo(requestId, filePath, fileName, jsonData.getBytes().length);
            
            logger.info("Successfully uploaded JSON data with history tracking for type: {} with request ID: {}", 
                       exportType, requestId);
            
            return ResponseEntity.ok(Map.of(
                "requestId", requestId,
                "filePath", filePath,
                "fileName", fileName,
                "message", "JSON data uploaded successfully with history tracking"
            ));
            
        } catch (Exception e) {
            logger.error("Failed to upload JSON data with history for export type: {}", exportType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload JSON data: " + e.getMessage()));
        }
    }
    
    private String extractFileName(String filePath) {
        if (filePath == null) return null;
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
}