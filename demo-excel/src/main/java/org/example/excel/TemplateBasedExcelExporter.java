package org.example.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TemplateBasedExcelExporter<T> extends BaseExcelExporter<T> {
    
    @Override
    public void exportData(List<T> data, String filePath) throws IOException {
        exportData(data, filePath, "Sheet1");
    }
    
    public void exportData(List<T> data, String filePath, String sheetName) throws IOException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be empty for template-based export");
        }
        
        createSheet(sheetName);
        
        T firstItem = data.get(0);
        List<String> fieldNames = getFieldNames(firstItem);
        List<String> headers = generateHeaders(fieldNames);
        
        createHeaderRow(headers.toArray(new String[0]));
        
        int rowIndex = 1;
        for (T item : data) {
            Row dataRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < fieldNames.size(); i++) {
                Cell cell = dataRow.createCell(i);
                Object value = getFieldValue(item, fieldNames.get(i));
                setCellValue(cell, value);
            }
        }
        
        for (int i = 0; i < fieldNames.size(); i++) {
            sheet.autoSizeColumn(i);
        }
        
        saveToFile(filePath);
    }
    
    private List<String> getFieldNames(T object) {
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }
    
    private List<String> generateHeaders(List<String> fieldNames) {
        List<String> headers = new ArrayList<>();
        for (String fieldName : fieldNames) {
            String header = convertFieldNameToHeader(fieldName);
            headers.add(header);
        }
        return headers;
    }
    
    private String convertFieldNameToHeader(String fieldName) {
        StringBuilder header = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : fieldName.toCharArray()) {
            if (c == '_') {
                header.append(' ');
                capitalizeNext = true;
            } else if (Character.isUpperCase(c) && header.length() > 0) {
                header.append(' ').append(c);
                capitalizeNext = false;
            } else if (capitalizeNext) {
                header.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                header.append(c);
            }
        }
        
        return header.toString();
    }
}
