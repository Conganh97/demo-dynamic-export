# Excel Export Service

A Spring Boot service for exporting data to Excel files using JXLS templates with async processing and MinIO storage.

## Features

- **Generic Export System**: Configurable export types with pluggable data providers
- **Async Export Processing**: Exports under 5 seconds return files immediately, longer exports process in background
- **MinIO Integration**: Large files are stored in MinIO for later download
- **Export Status Tracking**: Track export progress with task IDs
- **Template-based Generation**: Use JXLS templates for flexible Excel formatting
- **RESTful API**: Clean REST endpoints for all operations
- **Extensible Architecture**: Easy to add new export types without code changes
- **H2 Database**: In-memory database for export task tracking

## API Endpoints

### Async Export Endpoints

- `GET /excel-service/api/excel/export/employees?count=10` - Export employees (async with 5s timeout)
- `GET /excel-service/api/excel/export/products?count=10` - Export products (async with 5s timeout)
- `POST /excel-service/api/excel/export/{exportType}` - Generic export by type with parameters
- `POST /excel-service/api/excel/export/custom` - Export custom data using template

### Export Status & Download

- `GET /excel-service/api/excel/status/{taskId}` - Check export status
- `GET /excel-service/api/excel/download/{taskId}` - Download completed export file

### Sample Data Endpoints

- `GET /excel-service/api/excel/sample-data/employees?count=5` - Get sample employee data
- `GET /excel-service/api/excel/sample-data/products?count=5` - Get sample product data

### Health Check

- `GET /excel-service/api/excel/health` - Service health status

## Running the Application

### Prerequisites
- Java 17+
- Docker (for MinIO)

### Setup Steps

1. **Start MinIO (required for async exports)**:
   ```bash
   docker-compose up -d
   ```
   - MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
   - MinIO API: http://localhost:9000

2. **Run the Spring Boot application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Access the service**:
   - API Base URL: http://localhost:8080/excel-service/api/excel/
   - H2 Console: http://localhost:8080/excel-service/h2-console

## Export Examples

### Generic Export by Type
```json
POST /excel-service/api/excel/export/employees
{
  "count": 100,
  "department": "IT"
}
```

### Custom Template Export
```json
POST /excel-service/api/excel/export/custom
{
  "exportType": "custom_report",
  "templatePath": "templates/custom_template.xlsx",
  "fileName": "my_export.xlsx",
  "data": {
    "title": "My Custom Report",
    "items": [
      {"name": "Item 1", "value": 100},
      {"name": "Item 2", "value": 200}
    ]
  }
}
```

### Adding New Export Types
```java
@Configuration
public class MyExportConfiguration {
    
    @Autowired
    private ExportConfigRegistry exportConfigRegistry;
    
    @PostConstruct
    public void configureCustomExports() {
        ExportConfigRegistry.ExportConfig customConfig = new ExportConfigRegistry.ExportConfig(
            "templates/my_template.xlsx",
            "my_export",
            parameters -> {
                // Your custom data generation logic
                Map<String, Object> data = new HashMap<>();
                data.put("customData", generateMyData(parameters));
                return data;
            }
        );
        
        exportConfigRegistry.registerExportConfig("my_export_type", customConfig);
    }
}
```

## Template Structure

Templates should be placed in `src/main/resources/templates/` directory and use JXLS syntax:

- `${variable}` - Simple variable substitution
- `jx:each` - Loop through collections
- `jx:if` - Conditional rendering

## Async Export Flow

### Fast Export (< 5 seconds)
1. Request export endpoint
2. Receive file immediately in response

### Slow Export (> 5 seconds)
1. Request export endpoint
2. Receive task ID and status URL
3. Poll status endpoint to check progress
4. Download file when completed

### Example Response for Async Export
```json
{
  "taskId": "uuid-task-id",
  "message": "Export is taking longer than expected. Processing in background...",
  "status": "PROCESSING",
  "statusUrl": "/api/excel/status/uuid-task-id"
}
```

### Example Status Response
```json
{
  "taskId": "uuid-task-id",
  "status": "COMPLETED",
  "exportType": "employees",
  "recordCount": 1000,
  "createdAt": "2024-01-01T10:00:00",
  "completedAt": "2024-01-01T10:00:30",
  "downloadUrl": "/api/excel/download/uuid-task-id",
  "fileName": "employees_export_20240101_100030.xlsx"
}
```

## Dependencies

- Spring Boot 3.1.5
- Apache POI 5.2.5
- JXLS 2.12.0
- MinIO 8.5.7
- H2 Database
- Java 17+
