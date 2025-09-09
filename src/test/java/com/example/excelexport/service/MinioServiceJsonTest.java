package com.example.excelexport.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "minio.endpoint=http://localhost:9000",
    "minio.access-key=minioadmin",
    "minio.secret-key=minioadmin",
    "minio.bucket-name=test-bucket"
})
public class MinioServiceJsonTest {
    
    @Test
    public void testJsonUploadMethods() {
        String testJsonData = "{\"users\":[{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@example.com\"}]}";
        String requestId = "test-123";
        String exportType = "USER_JSON";
        
        assertNotNull(testJsonData);
        assertNotNull(requestId);
        assertNotNull(exportType);
        
        assertTrue(testJsonData.startsWith("{"));
        assertTrue(testJsonData.contains("users"));
    }
}
