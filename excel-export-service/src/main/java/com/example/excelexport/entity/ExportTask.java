package com.example.excelexport.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "export_tasks")
public class ExportTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String taskId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportStatus status;
    
    @Column(nullable = false)
    private String exportType;
    
    @Column
    private String fileName;
    
    @Column
    private String minioObjectName;
    
    @Column
    private String downloadUrl;
    
    @Column
    private String errorMessage;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private Integer recordCount;

    public enum ExportStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    public ExportTask() {
        this.createdAt = LocalDateTime.now();
        this.status = ExportStatus.PENDING;
    }

    public ExportTask(String taskId, String exportType) {
        this();
        this.taskId = taskId;
        this.exportType = exportType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ExportStatus getStatus() {
        return status;
    }

    public void setStatus(ExportStatus status) {
        this.status = status;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMinioObjectName() {
        return minioObjectName;
    }

    public void setMinioObjectName(String minioObjectName) {
        this.minioObjectName = minioObjectName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }
}
