package com.example.excelexport.config;

import com.example.excelexport.model.Employee;
import com.example.excelexport.model.Product;
import com.example.excelexport.service.ExportConfigRegistry;
import com.example.excelexport.service.MockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ExportConfiguration {

    @Autowired
    private ExportConfigRegistry exportConfigRegistry;
    
    @Autowired
    private MockDataService mockDataService;

    @PostConstruct
    public void configureExports() {
        registerEmployeeExport();
        registerProductExport();
    }

    private void registerEmployeeExport() {
        ExportConfigRegistry.ExportConfig employeeConfig = new ExportConfigRegistry.ExportConfig(
            "templates/employee_template.xlsx",
            "employees",
            parameters -> {
                int count = (Integer) parameters.getOrDefault("count", 10);
                List<Employee> employees = mockDataService.generateEmployees(count);
                
                Map<String, Object> data = new HashMap<>();
                data.put("employees", employees);
                data.put("title", "Employee Report");
                data.put("totalEmployees", employees.size());
                return data;
            }
        );
        
        exportConfigRegistry.registerExportConfig("employees", employeeConfig);
    }

    private void registerProductExport() {
        ExportConfigRegistry.ExportConfig productConfig = new ExportConfigRegistry.ExportConfig(
            "templates/product_template.xlsx",
            "products",
            parameters -> {
                int count = (Integer) parameters.getOrDefault("count", 10000);
                List<Product> products = mockDataService.generateProducts(count);
                
                Map<String, Object> data = new HashMap<>();
                data.put("products", products);
                data.put("title", "Product Catalog");
                data.put("totalProducts", products.size());
                return data;
            }
        );
        
        exportConfigRegistry.registerExportConfig("products", productConfig);
    }
}
