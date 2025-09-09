package org.example.model;

public class Employee {
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private double salary;
    private int age;
    
    public Employee() {}
    
    public Employee(String firstName, String lastName, String email, String department, double salary, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.age = age;
    }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
