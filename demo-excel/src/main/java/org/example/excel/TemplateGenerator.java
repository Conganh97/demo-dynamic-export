package org.example.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class TemplateGenerator {
    
    public static void createEmployeeTemplate(String templatePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employee Template");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "First Name", "Last Name", "Email Address", 
                "Department", "Annual Salary", "Age"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            addSampleDataRows(sheet, dataStyle);
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            try (FileOutputStream outputStream = new FileOutputStream(templatePath)) {
                workbook.write(outputStream);
            }
        }
    }
    
    public static void createProductTemplate(String templatePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Product Template");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Product Name", "Category", "Price", 
                "Stock Quantity", "Is Active"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            addProductSampleRows(sheet, dataStyle);
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }
            
            try (FileOutputStream outputStream = new FileOutputStream(templatePath)) {
                workbook.write(outputStream);
            }
        }
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setFontHeightInPoints((short) 12);
        
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return headerStyle;
    }
    
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return dataStyle;
    }
    
    private static void addSampleDataRows(Sheet sheet, CellStyle dataStyle) {
        String[][] sampleData = {
            {"John", "Doe", "john.doe@company.com", "IT", "75000", "30"},
            {"Jane", "Smith", "jane.smith@company.com", "HR", "65000", "28"}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < sampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                if (j == 4) {
                    cell.setCellValue(Double.parseDouble(sampleData[i][j]));
                } else if (j == 5) {
                    cell.setCellValue(Integer.parseInt(sampleData[i][j]));
                } else {
                    cell.setCellValue(sampleData[i][j]);
                }
                cell.setCellStyle(dataStyle);
            }
        }
    }
    
    private static void addProductSampleRows(Sheet sheet, CellStyle dataStyle) {
        Object[][] sampleData = {
            {"Laptop Pro", "Electronics", 1299.99, 50, true},
            {"Wireless Mouse", "Electronics", 29.99, 200, true}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < sampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                Object value = sampleData[i][j];
                
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                }
                cell.setCellStyle(dataStyle);
            }
        }
    }
}
