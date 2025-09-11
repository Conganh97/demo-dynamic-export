package com.example.excelexport.model;

import java.util.Map;

public class ExcelExportRequest {
    private String exportType;
    private String templatePath;
    private String fileName;
    private Map<String, Object> data;
    private Map<String, Object> parameters;

    public ExcelExportRequest() {}

    public ExcelExportRequest(String exportType, String templatePath, String fileName, Map<String, Object> data) {
        this.exportType = exportType;
        this.templatePath = templatePath;
        this.fileName = fileName;
        this.data = data;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
