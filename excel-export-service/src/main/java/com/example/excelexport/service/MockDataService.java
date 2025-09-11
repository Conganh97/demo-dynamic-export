package com.example.excelexport.service;

import com.example.excelexport.model.Employee;
import com.example.excelexport.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MockDataService {
    
    private static final String[] FIRST_NAMES = {
        "Nguyen", "Tran", "Le", "Pham", "Hoang", "Phan", "Vu", "Vo", "Dang", "Bui",
        "Do", "Ho", "Ngo", "Duong", "Ly", "Mai", "Trinh", "Lam", "Truong", "Dinh"
    };
    
    private static final String[] LAST_NAMES = {
        "Van A", "Thi B", "Van C", "Thi D", "Van E", "Thi F", "Van G", "Thi H", 
        "Van I", "Thi J", "Van K", "Thi L", "Van M", "Thi N", "Van O", "Thi P"
    };
    
    private static final String[] DEPARTMENTS = {
        "IT", "HR", "Finance", "Marketing", "Sales", "Operations", "R&D", "Legal"
    };
    
    private static final String[] PRODUCT_NAMES = {
        "Laptop Dell", "iPhone 15", "Samsung Galaxy", "MacBook Pro", "iPad Air",
        "Surface Pro", "AirPods", "Watch Series", "Gaming Mouse", "Keyboard"
    };
    
    private static final String[] CATEGORIES = {
        "Electronics", "Computers", "Mobile", "Accessories", "Gaming", "Audio"
    };
    
    private final Random random = new Random();

    public List<Employee> generateEmployees(int count) {
        List<Employee> employees = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase().replace(" ", "") + "@company.com";
            String department = DEPARTMENTS[random.nextInt(DEPARTMENTS.length)];
            BigDecimal salary = BigDecimal.valueOf(15000000 + random.nextInt(35000000));
            Integer age = 22 + random.nextInt(38);
            
            employees.add(new Employee(firstName, lastName, email, department, salary, age));
        }
        
        return employees;
    }
    
    public List<Product> generateProducts(int count) {
        List<Product> products = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String name = PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)] + " " + (i + 1);
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            BigDecimal price = BigDecimal.valueOf(100000 + random.nextInt(50000000));
            Integer stock = random.nextInt(1000);
            String description = "High quality " + name.toLowerCase() + " for professional use";
            
            products.add(new Product(name, category, price, stock, description));
        }
        
        return products;
    }
}
