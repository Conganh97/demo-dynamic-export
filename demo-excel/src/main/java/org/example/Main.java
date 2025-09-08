package org.example;

import org.example.data.MockDataGenerator;
import org.example.excel.HeaderBasedExcelExporter;
import org.example.excel.TemplateBasedExcelExporter;
import org.example.excel.ObjectHeaderBasedExcelExporter;
import org.example.excel.ObjectTemplateBasedExcelExporter;
import org.example.excel.TemplateConfigExcelExporter;
import org.example.excel.TemplateGenerator;
import org.example.model.Employee;
import org.example.model.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Excel Export Demo Starting...");
        
        try {
//            demonstrateMethod1();
//            demonstrateMethod2();
//            demonstrateObjectMethod1();
//            demonstrateObjectMethod2();
            demonstrateTemplateConfigMethod();
            System.out.println("Excel export demos completed successfully!");
        } catch (IOException e) {
            System.err.println("Error during Excel export: " + e.getMessage());
        }
    }
    
    private static void demonstrateMethod1() throws IOException {
        System.out.println("\n=== Method 1: Header-Based Export ===");
        
        List<Employee> employees = MockDataGenerator.generateEmployees();
        
        String[] headers = {"First Name", "Last Name", "Email Address", "Department", "Annual Salary", "Age"};
        String[] fieldNames = {"firstName", "lastName", "email", "department", "salary", "age"};
        
        HeaderBasedExcelExporter<Employee> exporter = new HeaderBasedExcelExporter<>(headers, fieldNames);
        exporter.exportData(employees, "employees_method1.xlsx", "Employee List");
        
        System.out.println("Employee data exported to: employees_method1.xlsx");
    }
    
    private static void demonstrateMethod2() throws IOException {
        System.out.println("\n=== Method 2: Template-Based Export ===");
        
        List<Product> products = MockDataGenerator.generateProducts();
        
        TemplateBasedExcelExporter<Product> exporter = new TemplateBasedExcelExporter<>();
        exporter.exportData(products, "products_method2.xlsx", "Product Catalog");
        
        System.out.println("Product data exported to: products_method2.xlsx");
        
        List<Employee> employees = MockDataGenerator.generateEmployees();
        
        TemplateBasedExcelExporter<Employee> employeeExporter = new TemplateBasedExcelExporter<>();
        employeeExporter.exportData(employees, "employees_method2.xlsx", "Employee Data");
        
        System.out.println("Employee data exported to: employees_method2.xlsx");
    }
    
    private static void demonstrateObjectMethod1() throws IOException {
        System.out.println("\n=== Object Method 1: Header-Based Export (Using Object) ===");
        
        List<Object> employees = new ArrayList<>(MockDataGenerator.generateEmployees());
        
        String[] headers = {"First Name", "Last Name", "Email Address", "Department", "Annual Salary", "Age"};
        String[] fieldNames = {"firstName", "lastName", "email", "department", "salary", "age"};
        
        ObjectHeaderBasedExcelExporter exporter = new ObjectHeaderBasedExcelExporter(headers, fieldNames);
        exporter.exportData(employees, "employees_object_method1.xlsx", "Employee List");
        
        System.out.println("Employee data exported to: employees_object_method1.xlsx");
    }
    
    private static void demonstrateObjectMethod2() throws IOException {
        System.out.println("\n=== Object Method 2: Template-Based Export (Using Object) ===");
        
        List<Object> products = new ArrayList<>(MockDataGenerator.generateProducts());
        
        ObjectTemplateBasedExcelExporter exporter = new ObjectTemplateBasedExcelExporter();
        exporter.exportData(products, "products_object_method2.xlsx", "Product Catalog");
        
        System.out.println("Product data exported to: products_object_method2.xlsx");
        
        List<Object> employees = new ArrayList<>(MockDataGenerator.generateEmployees());
        
        ObjectTemplateBasedExcelExporter employeeExporter = new ObjectTemplateBasedExcelExporter();
        employeeExporter.exportData(employees, "employees_object_method2.xlsx", "Employee Data");
        
        System.out.println("Employee data exported to: employees_object_method2.xlsx");
    }
    
    private static void demonstrateTemplateConfigMethod() throws IOException {
        System.out.println("\n=== Template Config Method: Using Excel Template Files ===");
        
        System.out.println("Creating template files...");
        TemplateGenerator.createEmployeeTemplate("employee_template.xlsx");
        TemplateGenerator.createProductTemplate("product_template.xlsx");
        System.out.println("Template files created: employee_template.xlsx, product_template.xlsx");
        
        System.out.println("\nExporting Employee data using template with manual mapping...");
        try (TemplateConfigExcelExporter employeeExporter = new TemplateConfigExcelExporter("employee_template.xlsx")) {
            Map<String, String> fieldMapping = new HashMap<>();
            fieldMapping.put("firstName", "First Name");
            fieldMapping.put("lastName", "Last Name");
            fieldMapping.put("email", "Email Address");
            fieldMapping.put("department", "Department");
            fieldMapping.put("salary", "Annual Salary");
            fieldMapping.put("age", "Age");
            
            employeeExporter.configureColumnMapping(fieldMapping);
            employeeExporter.setDataStartRow(1);
            
            List<Object> employees = new ArrayList<>(MockDataGenerator.generateEmployees());
            employeeExporter.exportData(employees, "employees_template_config.xlsx");
            System.out.println("Employee data exported to: employees_template_config.xlsx");
        }
        
        System.out.println("\nExporting Product data using template with auto mapping...");
        try (TemplateConfigExcelExporter productExporter = new TemplateConfigExcelExporter("product_template.xlsx")) {
            productExporter.setDataStartRow(1);
            
            List<Object> products = new ArrayList<>(MockDataGenerator.generateProducts());
            productExporter.exportData(products, "products_template_config.xlsx");
            System.out.println("Product data exported to: products_template_config.xlsx");
        }
    }
}