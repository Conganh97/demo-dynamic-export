package com.example.excelexport.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExportConfigRegistry {
    
    private final Map<String, ExportConfig> exportConfigs = new ConcurrentHashMap<>();
    
    public static class ExportConfig {
        private final String templatePath;
        private final String fileNamePrefix;
        private final ExcelDataProvider dataProvider;
        
        public ExportConfig(String templatePath, String fileNamePrefix, ExcelDataProvider dataProvider) {
            this.templatePath = templatePath;
            this.fileNamePrefix = fileNamePrefix;
            this.dataProvider = dataProvider;
        }
        
        public String getTemplatePath() { return templatePath; }
        public String getFileNamePrefix() { return fileNamePrefix; }
        public ExcelDataProvider getDataProvider() { return dataProvider; }
        
        public String generateFileName() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            return fileNamePrefix + "_export_" + timestamp + ".xlsx";
        }
    }
    
    public void registerExportConfig(String exportType, ExportConfig config) {
        exportConfigs.put(exportType, config);
    }
    
    public ExportConfig getExportConfig(String exportType) {
        return exportConfigs.get(exportType);
    }
    
    public boolean isExportTypeSupported(String exportType) {
        return exportConfigs.containsKey(exportType);
    }
    
    public Map<String, Object> generateExportData(String exportType, Map<String, Object> parameters) {
        ExportConfig config = getExportConfig(exportType);
        if (config == null) {
            throw new IllegalArgumentException("Unsupported export type: " + exportType);
        }
        
        Map<String, Object> data = config.getDataProvider().generateData(parameters);
        
        data.put("generatedDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        data.put("exportType", exportType);
        
        return data;
    }
}
