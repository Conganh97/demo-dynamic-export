package com.example.excelexport.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MinioService {
    
    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);
    
    private final MinioClient minioClient;
    private final String bucketName;
    
    public MinioService(@Value("${minio.endpoint}") String endpoint,
                       @Value("${minio.access-key}") String accessKey,
                       @Value("${minio.secret-key}") String secretKey,
                       @Value("${minio.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        
        createBucketIfNotExists();
    }
    
    private void createBucketIfNotExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                logger.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("Error creating bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to create MinIO bucket", e);
        }
    }
    
    public String uploadFile(String requestId, String exportType, byte[] fileData) {
        try {
            String fileName = generateFileName(requestId, exportType);
            String objectPath = "exports/" + fileName;
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .stream(new ByteArrayInputStream(fileData), fileData.length, -1)
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .build());
            
            logger.info("Successfully uploaded file: {} to MinIO", objectPath);
            return objectPath;
            
        } catch (Exception e) {
            logger.error("Failed to upload file to MinIO for request: {}", requestId, e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }
    
    public String uploadJsonFile(String requestId, String exportType, String jsonData) {
        try {
            String fileName = generateJsonFileName(requestId, exportType);
            String objectPath = "json-exports/" + fileName;
            byte[] jsonBytes = jsonData.getBytes("UTF-8");
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .stream(new ByteArrayInputStream(jsonBytes), jsonBytes.length, -1)
                    .contentType("application/json")
                    .build());
            
            logger.info("Successfully uploaded JSON file: {} to MinIO", objectPath);
            return objectPath;
            
        } catch (Exception e) {
            logger.error("Failed to upload JSON file to MinIO for request: {}", requestId, e);
            throw new RuntimeException("Failed to upload JSON file to MinIO", e);
        }
    }
    
    public String uploadJsonFile(String requestId, String exportType, byte[] jsonData) {
        try {
            String fileName = generateJsonFileName(requestId, exportType);
            String objectPath = "json-exports/" + fileName;
            
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .stream(new ByteArrayInputStream(jsonData), jsonData.length, -1)
                    .contentType("application/json")
                    .build());
            
            logger.info("Successfully uploaded JSON file: {} to MinIO", objectPath);
            return objectPath;
            
        } catch (Exception e) {
            logger.error("Failed to upload JSON file to MinIO for request: {}", requestId, e);
            throw new RuntimeException("Failed to upload JSON file to MinIO", e);
        }
    }
    
    public InputStream downloadFile(String filePath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
        } catch (Exception e) {
            logger.error("Failed to download file from MinIO: {}", filePath, e);
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }
    
    public void deleteFile(String filePath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            logger.info("Successfully deleted file: {} from MinIO", filePath);
        } catch (Exception e) {
            logger.error("Failed to delete file from MinIO: {}", filePath, e);
        }
    }
    
    public boolean fileExists(String filePath) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getFileUrl(String filePath, int expiryInSeconds) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(filePath)
                    .expiry(expiryInSeconds)
                    .build());
        } catch (Exception e) {
            logger.error("Failed to generate presigned URL for file: {}", filePath, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }
    
    private String generateFileName(String requestId, String exportType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s_%s.xlsx", exportType.toLowerCase(), requestId, timestamp);
    }
    
    private String generateJsonFileName(String requestId, String exportType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s_%s.json", exportType.toLowerCase(), requestId, timestamp);
    }
}

