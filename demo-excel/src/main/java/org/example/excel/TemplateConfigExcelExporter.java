package org.example.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateConfigExcelExporter implements AutoCloseable {
    
    private Workbook workbook;
    private Sheet sheet;
    private Map<String, Integer> columnMapping;
    private int dataStartRow;
    
    public TemplateConfigExcelExporter(String templatePath) throws IOException {
        loadTemplate(templatePath);
        this.columnMapping = new HashMap<>();
        this.dataStartRow = 1;
    }
    
    private void loadTemplate(String templatePath) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(templatePath)) {
            this.workbook = new XSSFWorkbook(inputStream);
            this.sheet = workbook.getSheetAt(0);
        }
    }
    
    public void configureColumnMapping(Map<String, String> fieldToColumnMapping) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalStateException("Template must have a header row at row 0");
        }
        
        for (Map.Entry<String, String> entry : fieldToColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            String columnHeader = entry.getValue();
            
            for (Cell cell : headerRow) {
                if (cell.getStringCellValue().trim().equals(columnHeader)) {
                    columnMapping.put(fieldName, cell.getColumnIndex());
                    break;
                }
            }
        }
    }
    
    public void autoConfigureColumnMapping(Object sampleObject) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalStateException("Template must have a header row at row 0");
        }
        
        Field[] fields = sampleObject.getClass().getDeclaredFields();
        
        for (Field field : fields) {
            String fieldName = field.getName();
            String expectedHeader = convertFieldNameToHeader(fieldName);
            
            for (Cell cell : headerRow) {
                String cellValue = cell.getStringCellValue().trim();
                if (cellValue.equalsIgnoreCase(expectedHeader) || 
                    cellValue.equalsIgnoreCase(fieldName)) {
                    columnMapping.put(fieldName, cell.getColumnIndex());
                    break;
                }
            }
        }
    }
    
    public void setDataStartRow(int startRow) {
        this.dataStartRow = startRow;
    }
    
    public void exportData(List<Object> data, String outputPath) throws IOException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be empty");
        }
        
        if (columnMapping.isEmpty()) {
            autoConfigureColumnMapping(data.get(0));
        }
        
        clearExistingData();
        
        int rowIndex = dataStartRow;
        for (Object item : data) {
            Row dataRow = sheet.getRow(rowIndex);
            if (dataRow == null) {
                dataRow = sheet.createRow(rowIndex);
            }
            
            for (Map.Entry<String, Integer> entry : columnMapping.entrySet()) {
                String fieldName = entry.getKey();
                int columnIndex = entry.getValue();
                
                Cell cell = dataRow.getCell(columnIndex);
                if (cell == null) {
                    cell = dataRow.createCell(columnIndex);
                }
                
                Object value = getFieldValue(item, fieldName);
                setCellValue(cell, value);
            }
            rowIndex++;
        }
        
        autoSizeColumns();
        saveToFile(outputPath);
    }
    
    private void clearExistingData() {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = dataStartRow; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }
    
    private void autoSizeColumns() {
        for (int columnIndex : columnMapping.values()) {
            sheet.autoSizeColumn(columnIndex);
        }
    }
    
    private Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            return "";
        }
    }
    
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
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
    
    private void saveToFile(String filePath) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        }
    }
    
    public void close() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }
}
