package com.example.excelexport.model;

import java.util.Map;

public class ExportRequest {
    private String typeExport;
    private Map<String, Object> paramFilter;
    private String requestId;
    
    public ExportRequest() {}
    
    public ExportRequest(String typeExport, Map<String, Object> paramFilter, String requestId) {
        this.typeExport = typeExport;
        this.paramFilter = paramFilter;
        this.requestId = requestId;
    }
    
    public String getTypeExport() {
        return typeExport;
    }
    
    public void setTypeExport(String typeExport) {
        this.typeExport = typeExport;
    }
    
    public Map<String, Object> getParamFilter() {
        return paramFilter;
    }
    
    public void setParamFilter(Map<String, Object> paramFilter) {
        this.paramFilter = paramFilter;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

