package com.example.excelexport.service.impl;

import com.example.excelexport.entity.User;
import com.example.excelexport.repository.UserRepository;
import com.example.excelexport.service.ExcelGenerator;
import com.example.excelexport.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserExportService extends ExportService<User> {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ExcelGenerator excelGenerator;
    
    @Override
    public String getSupportedExportType() {
        return "USER";
    }
    
    @Override
    public List<User> queryData(Map<String, Object> paramFilter) {
        if (paramFilter.containsKey("status")) {
            return userRepository.findByStatus((String) paramFilter.get("status"));
        }
        if (paramFilter.containsKey("department")) {
            return userRepository.findByDepartment((String) paramFilter.get("department"));
        }
        return userRepository.findAll();
    }
    
    @Override
    public Map<String, String> generateHeaders(List<User> data) {
        Map<String, String> headers = new LinkedHashMap<>();
        
        if (!data.isEmpty()) {
            User firstUser = data.get(0);
            Field[] fields = firstUser.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                String fieldName = field.getName();
                String headerLabel = convertToHeaderLabel(fieldName);
                headers.put(fieldName, headerLabel);
            }
        }
        
        return headers;
    }
    
    @Override
    public Object extractFieldValue(User item, String fieldName) {
        try {
            Field field = item.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(item);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    protected byte[] generateExcel(List<User> data, Map<String, String> headers) {
        return excelGenerator.generateExcel(data, headers, this);
    }
    
    @Override
    protected byte[] createEmptyExcel() {
        return excelGenerator.createEmptyExcel();
    }
    
    private String convertToHeaderLabel(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z])", "$1 $2")
                       .toUpperCase()
                       .replace("_", " ");
    }
}

