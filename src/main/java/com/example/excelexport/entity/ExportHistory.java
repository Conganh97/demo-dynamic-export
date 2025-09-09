package com.example.excelexport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "export_history")
public class ExportHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "request_id", unique = true, nullable = false)
    private String requestId;
    
    @Column(name = "time_request", nullable = false)
    private LocalDateTime timeRequest;
    
    @Column(name = "export_type", nullable = false)
    private String exportType;
    
    @Column(name = "param_filters", columnDefinition = "TEXT")
    private String paramFilters;
    
    @Column(name = "time_period")
    private String timePeriod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExportStatus status;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "completed_time")
    private LocalDateTime completedTime;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    public enum ExportStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        EXPIRED,
        FAILED
    }
    
    public ExportHistory() {
        this.timeRequest = LocalDateTime.now();
        this.status = ExportStatus.PENDING;
    }
    
    public ExportHistory(String requestId, String exportType, String paramFilters) {
        this();
        this.requestId = requestId;
        this.exportType = exportType;
        this.paramFilters = paramFilters;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public LocalDateTime getTimeRequest() {
        return timeRequest;
    }
    
    public void setTimeRequest(LocalDateTime timeRequest) {
        this.timeRequest = timeRequest;
    }
    
    public String getExportType() {
        return exportType;
    }
    
    public void setExportType(String exportType) {
        this.exportType = exportType;
    }
    
    public String getParamFilters() {
        return paramFilters;
    }
    
    public void setParamFilters(String paramFilters) {
        this.paramFilters = paramFilters;
    }
    
    public String getTimePeriod() {
        return timePeriod;
    }
    
    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }
    
    public ExportStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExportStatus status) {
        this.status = status;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public LocalDateTime getCompletedTime() {
        return completedTime;
    }
    
    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}

