package com.example.excelexport.controller;

import com.example.excelexport.entity.ExportTask;
import com.example.excelexport.model.ExcelExportRequest;
import com.example.excelexport.service.GenericExcelExportService;
import com.example.excelexport.service.ExcelWriter;
import com.example.excelexport.service.MinIOService;
import com.example.excelexport.service.MockDataService;
import com.example.excelexport.service.ThreadMonitoringService;
import com.example.excelexport.model.Employee;
import com.example.excelexport.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelWriter excelWriter;
    
    @Autowired
    private MockDataService mockDataService;
    
    @Autowired
    private GenericExcelExportService genericExcelExportService;
    
    @Autowired
    private MinIOService minioService;
    
    @Autowired
    private ThreadMonitoringService threadMonitoringService;

    @GetMapping("/export/employees")
    public CompletableFuture<ResponseEntity<?>> exportEmployees(@RequestParam(defaultValue = "10") int count) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("count", count);
        
        return genericExcelExportService.exportWithTimeout("employees", parameters, 1)
                .thenApply(result -> {
                    if (result.isImmediate()) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", result.getFileName());
                        return ResponseEntity.ok().headers(headers).body(result.getData());
                    } else {
                        Map<String, Object> response = new HashMap<>();
                        response.put("taskId", result.getTaskId());
                        response.put("message", result.getMessage());
                        response.put("status", "PROCESSING");
                        response.put("statusUrl", "/api/excel/status/" + result.getTaskId());
                        return ResponseEntity.accepted().body(response);
                    }
                });
    }

    @GetMapping("/export/products")
    public CompletableFuture<ResponseEntity<?>> exportProducts(@RequestParam(defaultValue = "10") int count) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("count", count);
        
        return genericExcelExportService.exportWithTimeout("products", parameters, 5)
                .thenApply(result -> {
                    if (result.isImmediate()) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", result.getFileName());
                        return ResponseEntity.ok().headers(headers).body(result.getData());
                    } else {
                        Map<String, Object> response = new HashMap<>();
                        response.put("taskId", result.getTaskId());
                        response.put("message", result.getMessage());
                        response.put("status", "PROCESSING");
                        response.put("statusUrl", "/api/excel/status/" + result.getTaskId());
                        return ResponseEntity.accepted().body(response);
                    }
                });
    }

    @PostMapping("/export/custom")
    public CompletableFuture<ResponseEntity<?>> exportCustomData(@RequestBody ExcelExportRequest request) {
        if (request.getTemplatePath() == null || request.getData() == null) {
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().build());
        }
        
        return genericExcelExportService.exportWithTimeout(request, 1)
                .thenApply(result -> {
                    if (result.isImmediate()) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", result.getFileName());
                        return ResponseEntity.ok().headers(headers).body(result.getData());
                    } else {
                        Map<String, Object> response = new HashMap<>();
                        response.put("taskId", result.getTaskId());
                        response.put("message", result.getMessage());
                        response.put("status", "PROCESSING");
                        response.put("statusUrl", "/api/excel/status/" + result.getTaskId());
                        return ResponseEntity.accepted().body(response);
                    }
                });
    }

    @PostMapping("/export/{exportType}")
    public CompletableFuture<ResponseEntity<?>> exportByType(
            @PathVariable String exportType,
            @RequestBody(required = false) Map<String, Object> parameters) {
        
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        
        return genericExcelExportService.exportWithTimeout(exportType, parameters, 5)
                .thenApply(result -> {
                    if (result.isImmediate()) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", result.getFileName());
                        return ResponseEntity.ok().headers(headers).body(result.getData());
                    } else {
                        Map<String, Object> response = new HashMap<>();
                        response.put("taskId", result.getTaskId());
                        response.put("message", result.getMessage());
                        response.put("status", "PROCESSING");
                        response.put("statusUrl", "/api/excel/status/" + result.getTaskId());
                        return ResponseEntity.accepted().body(response);
                    }
                });
    }

    @GetMapping("/sample-data/employees")
    public ResponseEntity<List<Employee>> getSampleEmployees(@RequestParam(defaultValue = "10000") int count) {
        List<Employee> employees = mockDataService.generateEmployees(count);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/sample-data/products")
    public ResponseEntity<List<Product>> getSampleProducts(@RequestParam(defaultValue = "5") int count) {
        List<Product> products = mockDataService.generateProducts(count);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/status/{taskId}")
    public ResponseEntity<Map<String, Object>> getExportStatus(@PathVariable String taskId) {
        ExportTask task = genericExcelExportService.getTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", task.getTaskId());
        response.put("status", task.getStatus().toString());
        response.put("exportType", task.getExportType());
        response.put("recordCount", task.getRecordCount());
        response.put("createdAt", task.getCreatedAt());
        response.put("completedAt", task.getCompletedAt());
        
        if (task.getStatus() == ExportTask.ExportStatus.COMPLETED) {
            response.put("downloadUrl", "/api/excel/download/" + taskId);
            response.put("fileName", task.getFileName());
        }
        
        if (task.getStatus() == ExportTask.ExportStatus.FAILED) {
            response.put("errorMessage", task.getErrorMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{taskId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String taskId) {
        try {
            ExportTask task = genericExcelExportService.getTaskStatus(taskId);
            if (task == null || task.getStatus() != ExportTask.ExportStatus.COMPLETED) {
                return ResponseEntity.notFound().build();
            }
            
            InputStream fileStream = minioService.downloadFile(task.getMinioObjectName());
            byte[] fileData = fileStream.readAllBytes();
            fileStream.close();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", task.getFileName());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Excel Export Service");
        status.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/monitor/threads")
    public ResponseEntity<Map<String, Object>> getThreadStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("threadPool", threadMonitoringService.getThreadPoolStatus());
        response.put("currentThread", threadMonitoringService.getCurrentThreadInfo());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monitor/test-async")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> testAsyncBehavior(
            @RequestParam(defaultValue = "100") int count) {
        
        String requestThread = Thread.currentThread().getName();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("count", count);
        
        Map<String, Object> testInfo = new HashMap<>();
        testInfo.put("message", "Testing async behavior with " + count + " records");
        testInfo.put("requestThread", requestThread);
        testInfo.put("timeout", "1 second");
        testInfo.put("expectedBehavior", count > 50 ? "Should timeout and go async" : "Should complete immediately");
        
        return genericExcelExportService.exportWithTimeout("employees", parameters, 1)
                .thenApply(result -> {
                    String responseThread = Thread.currentThread().getName();
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("testInfo", testInfo);
                    response.put("result", Map.of(
                        "taskId", result.getTaskId(),
                        "immediate", result.isImmediate(),
                        "message", result.getMessage(),
                        "responseThread", responseThread
                    ));
                    
                    if (!result.isImmediate()) {
                        response.put("statusUrl", "/api/excel/status/" + result.getTaskId());
                    }
                    
                    return ResponseEntity.ok(response);
                });
    }
}
