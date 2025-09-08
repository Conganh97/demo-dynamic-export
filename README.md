# Excel Export Service

A Spring Boot service that listens to Kafka messages and dynamically generates Excel files based on data queries.

## Architecture

### Core Components

1. **ExportKafkaListener** - Listens to Kafka messages with format `{typeExport}-{paramFilter}`
2. **ExportService** - Abstract service defining the export contract
3. **ExportServiceRegistry** - Registry pattern to manage multiple export types
4. **ExcelGenerator** - Utility to generate Excel files with dynamic headers
5. **Dynamic Header Generation** - Headers are created based on actual data structure

### Message Format

The service accepts Kafka messages in two formats:

1. **Simple Format**: `USER-{"status":"ACTIVE","department":"IT"}`
2. **JSON Format**: Full ExportRequest JSON object

### Supported Export Types

- `USER` - Exports user data with filtering by status/department

## How It Works

1. **Message Reception**: Kafka listener receives export request
2. **Service Resolution**: Registry finds appropriate export service by type
3. **Data Query**: Service queries data using provided filters
4. **Dynamic Headers**: Headers are generated from first data object's fields
5. **Excel Generation**: Apache POI creates Excel with dynamic structure
6. **Output**: Excel file is generated as byte array

## Usage Examples

### Kafka Message Examples

```bash
# Export all active users
USER-{"status":"ACTIVE"}

# Export users from IT department
USER-{"department":"IT"}

# Export all users (no filter)
USER-{}
```

### Adding New Export Types

1. Create entity and repository
2. Extend `ExportService<YourEntity>`
3. Implement required methods:
   - `getSupportedExportType()`
   - `queryData()`
   - `generateHeaders()`
   - `extractFieldValue()`

Example:
```java
@Service
public class ProductExportService extends ExportService<Product> {
    @Override
    public String getSupportedExportType() {
        return "PRODUCT";
    }
    
    @Override
    public List<Product> queryData(Map<String, Object> paramFilter) {
        // Your query logic
    }
    
    @Override
    public Map<String, String> generateHeaders(List<Product> data) {
        // Dynamic header generation
    }
    
    @Override
    public Object extractFieldValue(Product item, String fieldName) {
        // Field value extraction using reflection
    }
}
```

## Configuration

### Kafka Settings
```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: localhost:9092
      group-id: excel-export-group

app:
  kafka:
    export:
      topic: excel-export-requests
```

## Key Features

- **Dynamic Headers**: Headers automatically generated from data structure
- **Strategy Pattern**: Easy to add new export types
- **Reflection-based**: Field extraction using Java reflection
- **Kafka Integration**: Asynchronous message processing
- **Spring Boot**: Full Spring ecosystem integration
- **Apache POI**: Professional Excel generation
- **JPA/Hibernate**: Database abstraction layer

## New Features: MinIO Integration & Export History

### Export History Tracking
All export requests are now tracked in the database with:
- Time request
- Export type
- Parameter filters
- Time period
- Status (PENDING, PROCESSING, COMPLETED, EXPIRED, FAILED)
- File information and download capability

### MinIO File Storage
- Exported Excel files are automatically uploaded to MinIO
- Files are organized in `/exports/` folder with naming pattern: `{type}_{requestId}_{timestamp}.xlsx`
- Automatic cleanup of expired files (7 days retention)
- Presigned URL support for secure downloads

### REST API Endpoints

#### Get Export History
```http
GET /api/export/history
GET /api/export/history?exportType=USER
GET /api/export/history?status=COMPLETED
```

#### Get Specific Export
```http
GET /api/export/history/{requestId}
```

#### Download Excel File
```http
GET /api/export/download/{requestId}
```

#### Get Download URL (Presigned)
```http
GET /api/export/download-url/{requestId}?expiryInSeconds=3600
```

### Scheduled Tasks
- **Cleanup expired exports**: Runs hourly, removes files older than 7 days
- **Handle stuck processing**: Runs every 30 minutes, marks stalled exports as failed

## Infrastructure Setup

### Using Docker Compose
```bash
docker-compose up -d
```

This starts:
- Kafka + Zookeeper on port 9092
- MinIO on port 9000 (console on 9001)

### MinIO Console
Access MinIO console at: http://localhost:9001
- Username: minioadmin
- Password: minioadmin

## Message Processing Flow

1. **Kafka Message** → `ExportKafkaListener`
2. **Create History Record** → Status: PENDING
3. **Update Status** → PROCESSING
4. **Generate Excel** → Using dynamic headers
5. **Upload to MinIO** → Store in `/exports/` folder
6. **Update History** → Status: COMPLETED with file info
7. **Download Available** → Via REST API

## Database Schema

### Export History Table
```sql
CREATE TABLE export_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(255) UNIQUE NOT NULL,
    time_request TIMESTAMP NOT NULL,
    export_type VARCHAR(100) NOT NULL,
    param_filters TEXT,
    time_period VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    file_name VARCHAR(255),
    completed_time TIMESTAMP,
    error_message TEXT,
    file_size BIGINT
);
```

## Dependencies

- Spring Boot 3.2.0
- Spring Kafka
- Spring Data JPA
- Apache POI 5.2.4
- MinIO Client 8.5.7
- Java 17
