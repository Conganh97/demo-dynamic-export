package com.example.excelexport.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class TemplateService {

    public void createEmployeeTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("${title}");
        titleCell.setCellStyle(headerStyle);

        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Generated: ${generatedDate}");

        Row totalRow = sheet.createRow(2);
        Cell totalCell = totalRow.createCell(0);
        totalCell.setCellValue("Total Employees: ${totalEmployees}");

        Row headerRow = sheet.createRow(4);
        String[] headers = {"First Name", "Last Name", "Email", "Department", "Salary", "Age"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Row dataRow = sheet.createRow(5);
        dataRow.createCell(0).setCellValue("${employees[0].firstName}");
        dataRow.createCell(1).setCellValue("${employees[0].lastName}");
        dataRow.createCell(2).setCellValue("${employees[0].email}");
        dataRow.createCell(3).setCellValue("${employees[0].department}");
        dataRow.createCell(4).setCellValue("${employees[0].salary}");
        dataRow.createCell(5).setCellValue("${employees[0].age}");

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream outputStream = new FileOutputStream("src/main/resources/templates/employee_template.xlsx")) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    public void createProductTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("${title}");
        titleCell.setCellStyle(headerStyle);

        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Generated: ${generatedDate}");

        Row totalRow = sheet.createRow(2);
        Cell totalCell = totalRow.createCell(0);
        totalCell.setCellValue("Total Products: ${totalProducts}");

        Row headerRow = sheet.createRow(4);
        String[] headers = {"Product Name", "Category", "Price", "Stock", "Description"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Row dataRow = sheet.createRow(5);
        dataRow.createCell(0).setCellValue("${products[0].name}");
        dataRow.createCell(1).setCellValue("${products[0].category}");
        dataRow.createCell(2).setCellValue("${products[0].price}");
        dataRow.createCell(3).setCellValue("${products[0].stock}");
        dataRow.createCell(4).setCellValue("${products[0].description}");

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream outputStream = new FileOutputStream("src/main/resources/templates/product_template.xlsx")) {
            workbook.write(outputStream);
        }
        workbook.close();
    }
}
