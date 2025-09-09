package com.example.excelexport.service;

import com.example.excelexport.model.ExportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public abstract class JsonExportService<T> extends ExportService<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonExportService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String exportToJson(ExportRequest request) {
        if (!getSupportedExportType().equals(request.getTypeExport())) {
            throw new IllegalArgumentException("Unsupported export type: " + request.getTypeExport());
        }
        
        List<T> data = queryData(request.getParamFilter());
        return generateJson(data);
    }
    
    protected String generateJson(List<T> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            logger.error("Failed to generate JSON", e);
            throw new RuntimeException("Failed to generate JSON", e);
        }
    }
    
    protected String createEmptyJson() {
        return "[]";
    }
    
    @Override
    protected byte[] generateExcel(List<T> data, Map<String, String> headers) {
        throw new UnsupportedOperationException("Excel generation not supported in JsonExportService");
    }
    
    @Override
    protected byte[] createEmptyExcel() {
        throw new UnsupportedOperationException("Excel generation not supported in JsonExportService");
    }
}
