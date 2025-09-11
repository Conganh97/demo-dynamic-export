# Thread Monitoring Guide

## Cách kiểm tra có tách thread chạy hay không

### 1. Kiểm tra qua Logs

Khi chạy application, bạn sẽ thấy logs với thread names:

```
2024-01-01 10:00:00 [http-nio-8080-exec-1] INFO  c.e.e.service.GenericExcelExportService - 🚀 Starting export - TaskId: abc123, Type: employees, Thread: http-nio-8080-exec-1, Timeout: 1s

2024-01-01 10:00:00 [ForkJoinPool.commonPool-worker-1] INFO  c.e.e.service.GenericExcelExportService - 🔄 Processing export in worker thread - TaskId: abc123, Worker Thread: ForkJoinPool.commonPool-worker-1

2024-01-01 10:00:01 [http-nio-8080-exec-1] WARN  c.e.e.service.GenericExcelExportService - ⏰ Export TIMEOUT after 1000ms - TaskId: abc123, Handler Thread: http-nio-8080-exec-1, Starting async processing

2024-01-01 10:00:01 [ExportAsync-1] INFO  c.e.e.service.GenericExcelExportService - 🔀 Starting ASYNC export processing - TaskId: abc123, Async Thread: ExportAsync-1
```

**Ý nghĩa các thread:**
- `http-nio-8080-exec-1`: Thread xử lý HTTP request (main thread)
- `ForkJoinPool.commonPool-worker-1`: Thread worker của CompletableFuture
- `ExportAsync-1`: Thread async của Spring @Async

### 2. API Endpoints để Monitor

#### Kiểm tra Thread Pool Status
```bash
GET /excel-service/api/excel/monitor/threads
```

Response:
```json
{
  "threadPool": {
    "threadPoolType": "ThreadPoolTaskExecutor",
    "corePoolSize": 2,
    "maxPoolSize": 5,
    "activeCount": 1,
    "poolSize": 2,
    "queueSize": 0,
    "completedTaskCount": 5,
    "threadNamePrefix": "ExportAsync-"
  },
  "currentThread": {
    "currentThreadName": "http-nio-8080-exec-2",
    "isMainThread": false,
    "isAsyncThread": false,
    "activeThreadCount": 15
  }
}
```

#### Test Async Behavior
```bash
GET /excel-service/api/excel/monitor/test-async?count=100
```

Response cho fast export (count <= 50):
```json
{
  "testInfo": {
    "expectedBehavior": "Should complete immediately",
    "requestThread": "http-nio-8080-exec-1"
  },
  "result": {
    "immediate": true,
    "responseThread": "ForkJoinPool.commonPool-worker-1"
  }
}
```

Response cho slow export (count > 50):
```json
{
  "testInfo": {
    "expectedBehavior": "Should timeout and go async",
    "requestThread": "http-nio-8080-exec-1"
  },
  "result": {
    "immediate": false,
    "message": "Export is taking longer than expected. Processing in background...",
    "responseThread": "http-nio-8080-exec-1"
  },
  "statusUrl": "/api/excel/status/task-id"
}
```

### 3. Cách Test Thread Separation

#### Test 1: Fast Export (Immediate Response)
```bash
curl "http://localhost:8080/excel-service/api/excel/export/employees?count=10"
```
- Sẽ trả về file Excel ngay lập tức
- Log sẽ hiển thị cùng thread xử lý từ đầu đến cuối

#### Test 2: Slow Export (Async Processing)
```bash
curl "http://localhost:8080/excel-service/api/excel/export/employees?count=100"
```
- Sẽ trả về task ID và status URL
- Log sẽ hiển thị:
  1. HTTP thread bắt đầu request
  2. Worker thread xử lý export
  3. Timeout xảy ra, HTTP thread trả response
  4. Async thread tiếp tục xử lý và upload MinIO

#### Test 3: Monitor Thread Pool
```bash
curl "http://localhost:8080/excel-service/api/excel/monitor/threads"
```
- Xem trạng thái thread pool
- Kiểm tra số thread đang active

### 4. Các Indicator cho Thread Separation

✅ **Có tách thread khi:**
- Log hiển thị thread names khác nhau
- `activeCount` trong thread pool tăng lên
- Response trả về `immediate: false` với task ID
- Async logs xuất hiện sau khi HTTP response đã trả về

❌ **Không tách thread khi:**
- Tất cả logs có cùng thread name
- Response luôn trả về file Excel trực tiếp
- `activeCount` luôn là 0
- Không có async logs

### 5. Troubleshooting

#### Nếu không thấy async behavior:
1. Kiểm tra timeout (hiện tại là 1 giây)
2. Tăng số lượng records (count > 50)
3. Kiểm tra MinIO có chạy không
4. Xem logs có error không

#### Nếu thread pool không hoạt động:
1. Kiểm tra `@EnableAsync` annotation
2. Kiểm tra `AsyncConfig` configuration
3. Kiểm tra `@Async("exportTaskExecutor")` annotation

### 6. Performance Metrics

Logs sẽ hiển thị timing information:
- Export processing time
- MinIO upload time
- Total async processing time

Example:
```
📊 Excel data generated in 1500ms - TaskId: abc123, Worker Thread: ForkJoinPool.commonPool-worker-1
☁️ Uploading to MinIO - TaskId: abc123, File: employees_export_20240101_100000.xlsx, Thread: ExportAsync-1
🎉 ASYNC export COMPLETED in 3200ms - TaskId: abc123, Thread: ExportAsync-1, File uploaded to MinIO
```
