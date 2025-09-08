package org.example.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.util.List;

public class HeaderBasedExcelExporter<T> extends BaseExcelExporter<T> {
    
    private String[] headers;
    private String[] fieldNames;
    
    public HeaderBasedExcelExporter(String[] headers, String[] fieldNames) {
        super();
        if (headers.length != fieldNames.length) {
            throw new IllegalArgumentException("Headers and field names must have the same length");
        }
        this.headers = headers;
        this.fieldNames = fieldNames;
    }
    
    @Override
    public void exportData(List<T> data, String filePath) throws IOException {
        exportData(data, filePath, "Sheet1");
    }
    
    public void exportData(List<T> data, String filePath, String sheetName) throws IOException {
        createSheet(sheetName);
        createHeaderRow(headers);
        
        int rowIndex = 1;
        for (T item : data) {
            Row dataRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < fieldNames.length; i++) {
                Cell cell = dataRow.createCell(i);
                Object value = getFieldValue(item, fieldNames[i]);
                setCellValue(cell, value);
            }
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        saveToFile(filePath);
    }
}
