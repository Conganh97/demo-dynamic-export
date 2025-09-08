package com.example.excelexport.service;

import com.example.excelexport.model.ExportRequest;
import java.util.List;
import java.util.Map;

public abstract class ExportService<T> {
    
    public abstract String getSupportedExportType();
    
    public abstract List<T> queryData(Map<String, Object> paramFilter);
    
    public abstract Map<String, String> generateHeaders(List<T> data);
    
    public abstract Object extractFieldValue(T item, String fieldName);
    
    public final byte[] export(ExportRequest request) {
        if (!getSupportedExportType().equals(request.getTypeExport())) {
            throw new IllegalArgumentException("Unsupported export type: " + request.getTypeExport());
        }
        
        List<T> data = queryData(request.getParamFilter());
        if (data.isEmpty()) {
            return createEmptyExcel();
        }
        
        Map<String, String> headers = generateHeaders(data);
        return generateExcel(data, headers);
    }
    
    protected abstract byte[] generateExcel(List<T> data, Map<String, String> headers);
    
    protected abstract byte[] createEmptyExcel();
}
