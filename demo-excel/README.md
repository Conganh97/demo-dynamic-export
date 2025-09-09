# Excel Export Base System

A flexible Excel export system using Java 17 and Apache POI that provides multiple approaches for exporting list data to Excel files with highlighted headers, including template-based configuration.

## Features

- **Abstract base class** for reusable Excel export functionality
- **Multiple export methods** to suit different needs
- **Template-based configuration** using pre-designed Excel files
- **Highlighted headers** with professional styling
- **Generic support** OR **Object-based support** for any object type
- **Auto-sizing columns** for optimal display
- **Clean separation** of mock data and source code

## Project Structure

```
src/main/java/
├── org/example/
│   ├── Main.java                                # Demo application
│   ├── data/
│   │   └── MockDataGenerator.java               # Sample data generation
│   ├── excel/
│   │   ├── BaseExcelExporter.java               # Generic abstract base class
│   │   ├── HeaderBasedExcelExporter.java        # Generic Method 1 implementation
│   │   ├── TemplateBasedExcelExporter.java      # Generic Method 2 implementation
│   │   ├── ObjectBaseExcelExporter.java         # Object abstract base class
│   │   ├── ObjectHeaderBasedExcelExporter.java  # Object Method 1 implementation
│   │   ├── ObjectTemplateBasedExcelExporter.java# Object Method 2 implementation
│   │   ├── TemplateConfigExcelExporter.java     # Template-based config exporter
│   │   └── TemplateGenerator.java               # Helper to create Excel templates
│   └── model/
│       ├── Employee.java                        # Sample model class
│       └── Product.java                         # Sample model class
```

## Usage

You can choose between **Generic** or **Object-based** approaches:

### Generic Approach (Type-Safe)

#### Method 1: Header-Based Export (Manual Headers)

```java
// Define custom headers and corresponding field names
String[] headers = {"First Name", "Last Name", "Email Address", "Department", "Annual Salary", "Age"};
String[] fieldNames = {"firstName", "lastName", "email", "department", "salary", "age"};

// Create exporter with custom headers
HeaderBasedExcelExporter<Employee> exporter = new HeaderBasedExcelExporter<>(headers, fieldNames);

// Export data
List<Employee> employees = MockDataGenerator.generateEmployees();
exporter.exportData(employees, "employees_method1.xlsx", "Employee List");
```

#### Method 2: Template-Based Export (Auto-Generated Headers)

```java
// Create template-based exporter
TemplateBasedExcelExporter<Product> exporter = new TemplateBasedExcelExporter<>();

// Export data (headers auto-generated from field names)
List<Product> products = MockDataGenerator.generateProducts();
exporter.exportData(products, "products_method2.xlsx", "Product Catalog");
```

### Object-Based Approach (More Flexible)

#### Object Method 1: Header-Based Export

```java
// Define custom headers and corresponding field names
String[] headers = {"First Name", "Last Name", "Email Address", "Department", "Annual Salary", "Age"};
String[] fieldNames = {"firstName", "lastName", "email", "department", "salary", "age"};

// Create Object-based exporter
ObjectHeaderBasedExcelExporter exporter = new ObjectHeaderBasedExcelExporter(headers, fieldNames);

// Export data (convert to List<Object>)
List<Object> employees = new ArrayList<>(MockDataGenerator.generateEmployees());
exporter.exportData(employees, "employees_object_method1.xlsx", "Employee List");
```

#### Object Method 2: Template-Based Export

```java
// Create Object-based template exporter
ObjectTemplateBasedExcelExporter exporter = new ObjectTemplateBasedExcelExporter();

// Export data (convert to List<Object>)
List<Object> products = new ArrayList<>(MockDataGenerator.generateProducts());
exporter.exportData(products, "products_object_method2.xlsx", "Product Catalog");
```

### Template-Based Configuration (Using Excel Templates)

This approach allows you to design your Excel template with custom formatting and use it for data export:

```java
// Step 1: Create or use existing Excel template
TemplateGenerator.createEmployeeTemplate("employee_template.xlsx");

// Step 2: Configure exporter with template
try (TemplateConfigExcelExporter exporter = new TemplateConfigExcelExporter("employee_template.xlsx")) {
    
    // Option A: Manual field mapping
    Map<String, String> fieldMapping = new HashMap<>();
    fieldMapping.put("firstName", "First Name");
    fieldMapping.put("lastName", "Last Name");
    fieldMapping.put("email", "Email Address");
    exporter.configureColumnMapping(fieldMapping);
    
    // Option B: Auto mapping (matches field names to headers automatically)
    // exporter.autoConfigureColumnMapping(sampleObject);
    
    // Set where data should start (row 1 = after headers)
    exporter.setDataStartRow(1);
    
    // Export data
    List<Object> employees = new ArrayList<>(MockDataGenerator.generateEmployees());
    exporter.exportData(employees, "employees_template_config.xlsx");
}
```

## Header Styling

Both methods automatically apply professional header styling:
- **Bold white text** on dark blue background
- **Centered alignment**
- **Borders** around all cells
- **Auto-sized columns** for optimal width

## Dependencies

The project uses Apache POI 5.2.5 for Excel file manipulation:

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

## Running the Demo

Execute the `Main` class to see both export methods in action:

```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

This will generate sample Excel files demonstrating both export approaches.

## Approach Comparison

| Feature | Generic | Object-Based | Template Config |
|---------|---------|--------------|-----------------|
| **Type Safety** | ✅ Compile-time | ❌ Runtime only | ❌ Runtime only |
| **Flexibility** | ❌ Fixed type | ✅ Mixed types | ✅ Mixed types |
| **Custom Formatting** | ❌ Code-defined | ❌ Code-defined | ✅ Excel template |
| **Performance** | ✅ Best | ❌ Casting overhead | ❌ File I/O overhead |
| **Ease of Design** | ❌ Code changes | ❌ Code changes | ✅ Excel design |
| **Reusability** | ✅ High | ✅ High | ✅ Template reuse |

## Extending the System

To use with your own model classes:

1. **Create your model class** with appropriate fields and getters/setters
2. **Choose your approach**:
   - **Generic**: Use `HeaderBasedExcelExporter<T>` or `TemplateBasedExcelExporter<T>`
   - **Object**: Use `ObjectHeaderBasedExcelExporter` or `ObjectTemplateBasedExcelExporter`
   - **Template Config**: Use `TemplateConfigExcelExporter` with pre-designed Excel templates
3. **Export your data** using the chosen exporter

Both approaches work with any object type thanks to Java reflection.
