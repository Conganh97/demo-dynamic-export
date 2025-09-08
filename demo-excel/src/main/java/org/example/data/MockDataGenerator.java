package org.example.data;

import org.example.model.Employee;
import org.example.model.Product;

import java.util.Arrays;
import java.util.List;

public class MockDataGenerator {
    
    public static List<Employee> generateEmployees() {
        return Arrays.asList(
            new Employee("John", "Doe", "john.doe@company.com", "IT", 75000.0, 30),
            new Employee("Jane", "Smith", "jane.smith@company.com", "HR", 65000.0, 28),
            new Employee("Bob", "Johnson", "bob.johnson@company.com", "Finance", 80000.0, 35),
            new Employee("Alice", "Brown", "alice.brown@company.com", "IT", 70000.0, 26),
            new Employee("Michael", "Wilson", "michael.wilson@company.com", "Marketing", 68000.0, 32),
            new Employee("Sarah", "Davis", "sarah.davis@company.com", "Sales", 72000.0, 29)
        );
    }
    
    public static List<Product> generateProducts() {
        return Arrays.asList(
            new Product("Laptop Pro", "Electronics", 1299.99, 50, true),
            new Product("Wireless Mouse", "Electronics", 29.99, 200, true),
            new Product("Office Chair", "Furniture", 199.99, 25, true),
            new Product("Desk Lamp", "Furniture", 49.99, 75, false),
            new Product("Smartphone", "Electronics", 699.99, 100, true),
            new Product("Tablet", "Electronics", 399.99, 80, true),
            new Product("Standing Desk", "Furniture", 299.99, 15, true)
        );
    }
}
