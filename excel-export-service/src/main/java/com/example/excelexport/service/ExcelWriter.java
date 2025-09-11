package com.example.excelexport.service;

import java.io.OutputStream;
import java.util.Map;

public interface ExcelWriter {
    void writeFromTemplate(OutputStream outStream, String templateName, Map<String, Object> data);
    
    byte[] writeFromTemplateToByte(String pathTemplate, Map<String, Object> data);
}
