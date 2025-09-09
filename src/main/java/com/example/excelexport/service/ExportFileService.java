package com.example.excelexport.service;

import com.example.excelexport.model.ExportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExportFileService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExportFileService.class);
    
    private final ExportServiceRegistry exportServiceRegistry;
    private final MinioService minioService;
    
    @Autowired
    public ExportFileService(ExportServiceRegistry exportServiceRegistry, MinioService minioService) {
        this.exportServiceRegistry = exportServiceRegistry;
        this.minioService = minioService;
    }
    
    public String exportToExcelAndUpload(ExportRequest request) {
        try {
            ExportService<?> exportService = exportServiceRegistry.getExportService(request.getTypeExport());
            byte[] excelData = exportService.export(request);
            
            return minioService.uploadFile(request.getRequestId(), request.getTypeExport(), excelData);
        } catch (Exception e) {
            logger.error("Failed to export Excel and upload to MinIO for request: {}", request.getRequestId(), e);
            throw new RuntimeException("Failed to export Excel and upload", e);
        }
    }
    
    public String exportToJsonAndUpload(ExportRequest request) {
        try {
            ExportService<?> exportService = exportServiceRegistry.getExportService(request.getTypeExport());
            
            if (!(exportService instanceof JsonExportService)) {
                throw new IllegalArgumentException("Export service does not support JSON export: " + request.getTypeExport());
            }
            
            JsonExportService<?> jsonExportService = (JsonExportService<?>) exportService;
            String jsonData = jsonExportService.exportToJson(request);
            
            return minioService.uploadJsonFile(request.getRequestId(), request.getTypeExport(), jsonData);
        } catch (Exception e) {
            logger.error("Failed to export JSON and upload to MinIO for request: {}", request.getRequestId(), e);
            throw new RuntimeException("Failed to export JSON and upload", e);
        }
    }
    
    public String uploadJsonData(String requestId, String exportType, String jsonData) {
        try {
            return minioService.uploadJsonFile(requestId, exportType, jsonData);
        } catch (Exception e) {
            logger.error("Failed to upload JSON data to MinIO for request: {}", requestId, e);
            throw new RuntimeException("Failed to upload JSON data", e);
        }
    }
    
    public String uploadJsonData(String requestId, String exportType, byte[] jsonData) {
        try {
            return minioService.uploadJsonFile(requestId, exportType, jsonData);
        } catch (Exception e) {
            logger.error("Failed to upload JSON data to MinIO for request: {}", requestId, e);
            throw new RuntimeException("Failed to upload JSON data", e);
        }
    }
}
