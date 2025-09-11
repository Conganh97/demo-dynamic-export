package com.example.excelexport.service;

import com.example.excelexport.entity.ExportTask;
import com.example.excelexport.model.ExcelExportRequest;
import com.example.excelexport.repository.ExportTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GenericExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(GenericExcelExportService.class);

    @Autowired
    private ExcelWriter excelWriter;
    
    @Autowired
    private MinIOService minioService;
    
    @Autowired
    private ExportTaskRepository exportTaskRepository;
    
    @Autowired
    private ExportConfigRegistry exportConfigRegistry;

    public CompletableFuture<ExportResult> exportWithTimeout(ExcelExportRequest request, long timeoutSeconds) {
        String taskId = UUID.randomUUID().toString();
        String currentThread = Thread.currentThread().getName();
        
        logger.info("🚀 Starting export - TaskId: {}, Type: {}, Thread: {}, Timeout: {}s", 
                   taskId, request.getExportType(), currentThread, timeoutSeconds);
        
        ExportTask task = new ExportTask(taskId, request.getExportType());
        if (request.getParameters() != null && request.getParameters().containsKey("count")) {
            task.setRecordCount((Integer) request.getParameters().get("count"));
        }
        exportTaskRepository.save(task);

        long startTime = System.currentTimeMillis();
        CompletableFuture<ExportResult> exportFuture = performExport(taskId, request);
        
        return exportFuture.orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .handle((result, throwable) -> {
                    long duration = System.currentTimeMillis() - startTime;
                    String handlerThread = Thread.currentThread().getName();
                    
                    if (throwable != null) {
                        if (throwable instanceof java.util.concurrent.TimeoutException) {
                            logger.warn("⏰ Export TIMEOUT after {}ms - TaskId: {}, Handler Thread: {}, Starting async processing", 
                                       duration, taskId, handlerThread);
                            processAsyncExport(taskId, request);
                            return new ExportResult(taskId, false, "Export is taking longer than expected. Processing in background...", null);
                        } else {
                            logger.error("❌ Export FAILED after {}ms - TaskId: {}, Handler Thread: {}, Error: {}", 
                                        duration, taskId, handlerThread, throwable.getMessage());
                            updateTaskStatus(taskId, ExportTask.ExportStatus.FAILED, throwable.getMessage());
                            return new ExportResult(taskId, false, "Export failed: " + throwable.getMessage(), null);
                        }
                    }
                    
                    logger.info("✅ Export COMPLETED immediately after {}ms - TaskId: {}, Handler Thread: {}", 
                               duration, taskId, handlerThread);
                    return result;
                });
    }

    public CompletableFuture<ExportResult> exportWithTimeout(String exportType, Map<String, Object> parameters, long timeoutSeconds) {
        ExcelExportRequest request = createExportRequest(exportType, parameters);
        return exportWithTimeout(request, timeoutSeconds);
    }

    private CompletableFuture<ExportResult> performExport(String taskId, ExcelExportRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String workerThread = Thread.currentThread().getName();
            long startTime = System.currentTimeMillis();
            
            logger.info("🔄 Processing export in worker thread - TaskId: {}, Worker Thread: {}", taskId, workerThread);
            
            try {
                updateTaskStatus(taskId, ExportTask.ExportStatus.PROCESSING, null);
                
                // Simulate some processing time to test async behavior
                if (request.getParameters() != null && request.getParameters().containsKey("count")) {
                    int count = (Integer) request.getParameters().get("count");
                    if (count > 50) {
                        logger.info("💤 Simulating slow processing for {} records - TaskId: {}", count, taskId);
                        Thread.sleep(2000); // Simulate slow processing for large datasets
                    }
                }
                
                byte[] excelData = generateExcelData(request);
                String fileName = determineFileName(request);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("📊 Excel data generated in {}ms - TaskId: {}, Worker Thread: {}, File: {}", 
                           duration, taskId, workerThread, fileName);
                
                updateTaskStatus(taskId, ExportTask.ExportStatus.COMPLETED, null);
                updateTaskFileName(taskId, fileName);
                
                return new ExportResult(taskId, true, "Export completed successfully", excelData, fileName);
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                logger.error("💥 Export processing failed after {}ms - TaskId: {}, Worker Thread: {}, Error: {}", 
                            duration, taskId, workerThread, e.getMessage());
                updateTaskStatus(taskId, ExportTask.ExportStatus.FAILED, e.getMessage());
                throw new RuntimeException("Export failed", e);
            }
        });
    }

    @Async("exportTaskExecutor")
    public void processAsyncExport(String taskId, ExcelExportRequest request) {
        String asyncThread = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        
        logger.info("🔀 Starting ASYNC export processing - TaskId: {}, Async Thread: {}", taskId, asyncThread);
        
        try {
            updateTaskStatus(taskId, ExportTask.ExportStatus.PROCESSING, null);
            
            byte[] excelData = generateExcelData(request);
            String fileName = determineFileName(request);
            
            long dataGenTime = System.currentTimeMillis() - startTime;
            logger.info("📊 Async Excel data generated in {}ms - TaskId: {}, Async Thread: {}", 
                       dataGenTime, taskId, asyncThread);
            
            logger.info("☁️ Uploading to MinIO - TaskId: {}, File: {}, Thread: {}", taskId, fileName, asyncThread);
            String objectName = minioService.uploadFile(excelData, fileName, 
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String downloadUrl = minioService.getDownloadUrl(objectName);
            
            ExportTask task = exportTaskRepository.findByTaskId(taskId).orElseThrow();
            task.setStatus(ExportTask.ExportStatus.COMPLETED);
            task.setFileName(fileName);
            task.setMinioObjectName(objectName);
            task.setDownloadUrl(downloadUrl);
            task.setCompletedAt(LocalDateTime.now());
            exportTaskRepository.save(task);
            
            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("🎉 ASYNC export COMPLETED in {}ms - TaskId: {}, Thread: {}, File uploaded to MinIO", 
                       totalTime, taskId, asyncThread);
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - startTime;
            logger.error("💥 ASYNC export FAILED after {}ms - TaskId: {}, Thread: {}, Error: {}", 
                        totalTime, taskId, asyncThread, e.getMessage());
            updateTaskStatus(taskId, ExportTask.ExportStatus.FAILED, e.getMessage());
        }
    }

    private byte[] generateExcelData(ExcelExportRequest request) {
        String templatePath = request.getTemplatePath();
        Map<String, Object> data = request.getData();
        
        if (templatePath == null && request.getExportType() != null) {
            ExportConfigRegistry.ExportConfig config = exportConfigRegistry.getExportConfig(request.getExportType());
            if (config != null) {
                templatePath = config.getTemplatePath();
                if (data == null) {
                    data = exportConfigRegistry.generateExportData(request.getExportType(), request.getParameters());
                }
            }
        }
        
        if (templatePath == null) {
            throw new IllegalArgumentException("Template path not specified and no configuration found for export type: " + request.getExportType());
        }
        
        if (data == null) {
            throw new IllegalArgumentException("No data provided for export");
        }
        
        return excelWriter.writeFromTemplateToByte(templatePath, data);
    }

    private String determineFileName(ExcelExportRequest request) {
        if (request.getFileName() != null) {
            return request.getFileName();
        }
        
        if (request.getExportType() != null) {
            ExportConfigRegistry.ExportConfig config = exportConfigRegistry.getExportConfig(request.getExportType());
            if (config != null) {
                return config.generateFileName();
            }
        }
        
        return "export_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
    }

    private ExcelExportRequest createExportRequest(String exportType, Map<String, Object> parameters) {
        ExcelExportRequest request = new ExcelExportRequest();
        request.setExportType(exportType);
        request.setParameters(parameters);
        return request;
    }

    private void updateTaskStatus(String taskId, ExportTask.ExportStatus status, String errorMessage) {
        ExportTask task = exportTaskRepository.findByTaskId(taskId).orElseThrow();
        task.setStatus(status);
        if (errorMessage != null) {
            task.setErrorMessage(errorMessage);
        }
        if (status == ExportTask.ExportStatus.COMPLETED || status == ExportTask.ExportStatus.FAILED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        exportTaskRepository.save(task);
    }

    private void updateTaskFileName(String taskId, String fileName) {
        ExportTask task = exportTaskRepository.findByTaskId(taskId).orElseThrow();
        task.setFileName(fileName);
        exportTaskRepository.save(task);
    }

    public ExportTask getTaskStatus(String taskId) {
        return exportTaskRepository.findByTaskId(taskId).orElse(null);
    }

    public static class ExportResult {
        private final String taskId;
        private final boolean immediate;
        private final String message;
        private final byte[] data;
        private final String fileName;

        public ExportResult(String taskId, boolean immediate, String message, byte[] data) {
            this(taskId, immediate, message, data, null);
        }

        public ExportResult(String taskId, boolean immediate, String message, byte[] data, String fileName) {
            this.taskId = taskId;
            this.immediate = immediate;
            this.message = message;
            this.data = data;
            this.fileName = fileName;
        }

        public String getTaskId() { return taskId; }
        public boolean isImmediate() { return immediate; }
        public String getMessage() { return message; }
        public byte[] getData() { return data; }
        public String getFileName() { return fileName; }
    }
}
