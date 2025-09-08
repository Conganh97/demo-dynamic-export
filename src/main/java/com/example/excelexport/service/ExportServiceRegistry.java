package com.example.excelexport.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExportServiceRegistry {
    
    private final Map<String, ExportService<?>> exportServices = new HashMap<>();
    
    @Autowired
    public ExportServiceRegistry(List<ExportService<?>> services) {
        for (ExportService<?> service : services) {
            exportServices.put(service.getSupportedExportType(), service);
        }
    }
    
    public ExportService<?> getExportService(String exportType) {
        ExportService<?> service = exportServices.get(exportType);
        if (service == null) {
            throw new IllegalArgumentException("No export service found for type: " + exportType);
        }
        return service;
    }
    
    public boolean isSupported(String exportType) {
        return exportServices.containsKey(exportType);
    }
}
