package com.example.excelexport.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class ExcelGenerator {
    
    public <T> byte[] generateExcel(List<T> data, Map<String, String> headers, 
                                  ExportService<T> exportService) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Export Data");
            
            createHeaderRow(sheet, headers, workbook);
            
            populateDataRows(sheet, data, headers, exportService);
            
            autoSizeColumns(sheet, headers.size());
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    private void createHeaderRow(Sheet sheet, Map<String, String> headers, Workbook workbook) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderCellStyle(workbook);
        
        int columnIndex = 0;
        for (String headerLabel : headers.values()) {
            Cell cell = headerRow.createCell(columnIndex++);
            cell.setCellValue(headerLabel);
            cell.setCellStyle(headerStyle);
        }
    }
    
    private <T> void populateDataRows(Sheet sheet, List<T> data, Map<String, String> headers, 
                                    ExportService<T> exportService) {
        int rowIndex = 1;
        for (T item : data) {
            Row row = sheet.createRow(rowIndex++);
            int columnIndex = 0;
            
            for (String fieldName : headers.keySet()) {
                Cell cell = row.createCell(columnIndex++);
                Object value = exportService.extractFieldValue(item, fieldName);
                setCellValue(cell, value);
            }
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
    
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    public byte[] createEmptyExcel() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("No Data");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("No data available");
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to create empty Excel file", e);
        }
    }
}
