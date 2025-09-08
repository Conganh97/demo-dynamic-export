package com.example.excelexport.listener;

import com.example.excelexport.entity.ExportHistory;
import com.example.excelexport.model.ExportRequest;
import com.example.excelexport.service.ExportServiceRegistry;
import com.example.excelexport.service.ExportService;
import com.example.excelexport.service.ExportHistoryService;
import com.example.excelexport.service.MinioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExportKafkaListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportKafkaListener.class);
    
    @Autowired
    private ExportServiceRegistry exportServiceRegistry;
    
    @Autowired
    private ExportHistoryService exportHistoryService;
    
    @Autowired
    private MinioService minioService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @KafkaListener(topics = "${app.kafka.export.topic:excel-export-requests}")
    public void handleExportRequest(String message) {
        ExportRequest request = null;
        try {
            logger.info("Received export request message: {}", message);
            
            request = parseMessage(message);
            
            ExportHistory history = exportHistoryService.createExportHistory(
                request.getRequestId(), 
                request.getTypeExport(), 
                request.getParamFilter()
            );
            
            if (!exportServiceRegistry.isSupported(request.getTypeExport())) {
                logger.error("Unsupported export type: {}", request.getTypeExport());
                exportHistoryService.updateWithError(request.getRequestId(), 
                    "Unsupported export type: " + request.getTypeExport());
                return;
            }
            
            exportHistoryService.updateStatus(request.getRequestId(), ExportHistory.ExportStatus.PROCESSING);
            
            ExportService<?> exportService = exportServiceRegistry.getExportService(request.getTypeExport());
            byte[] excelData = exportService.export(request);
            
            String filePath = minioService.uploadFile(request.getRequestId(), request.getTypeExport(), excelData);
            String fileName = extractFileName(filePath);
            
            exportHistoryService.updateWithFileInfo(request.getRequestId(), filePath, fileName, excelData.length);
            
            logger.info("Successfully generated and uploaded Excel for request: {} with {} bytes to {}", 
                       request.getRequestId(), excelData.length, filePath);
            
        } catch (Exception e) {
            logger.error("Failed to process export request: {}", message, e);
            if (request != null) {
                exportHistoryService.updateWithError(request.getRequestId(), e.getMessage());
            }
        }
    }
    
    private ExportRequest parseMessage(String message) {
        try {
            if (message.contains("-")) {
                String[] parts = message.split("-", 2);
                String typeExport = parts[0];
                String paramFilterJson = parts.length > 1 ? parts[1] : "{}";
                
                Map<String, Object> paramFilter = objectMapper.readValue(paramFilterJson, Map.class);
                
                return new ExportRequest(typeExport, paramFilter, generateRequestId());
            } else {
                return objectMapper.readValue(message, ExportRequest.class);
            }
        } catch (Exception e) {
            logger.error("Failed to parse message: {}", message, e);
            return new ExportRequest(message, new HashMap<>(), generateRequestId());
        }
    }
    
    private String generateRequestId() {
        return "REQ_" + System.currentTimeMillis();
    }
    
    private String extractFileName(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        return lastSlashIndex >= 0 ? filePath.substring(lastSlashIndex + 1) : filePath;
    }
}
