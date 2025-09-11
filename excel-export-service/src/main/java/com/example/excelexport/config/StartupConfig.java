package com.example.excelexport.config;

import com.example.excelexport.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class StartupConfig implements CommandLineRunner {

    @Autowired
    private TemplateService templateService;

    @Override
    public void run(String... args) throws Exception {
        File templatesDir = new File("src/main/resources/templates");
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
        }

        File employeeTemplate = new File("src/main/resources/templates/employee_template.xlsx");
        if (!employeeTemplate.exists()) {
            templateService.createEmployeeTemplate();
            System.out.println("Created employee template: " + employeeTemplate.getAbsolutePath());
        }

        File productTemplate = new File("src/main/resources/templates/product_template.xlsx");
        if (!productTemplate.exists()) {
            templateService.createProductTemplate();
            System.out.println("Created product template: " + productTemplate.getAbsolutePath());
        }

        System.out.println("Excel Export Service started successfully!");
        System.out.println("API available at: http://localhost:8080/excel-service/api/excel/");
    }
}
