CREATE TABLE IF NOT EXISTS export_history (
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

CREATE INDEX IF NOT EXISTS idx_export_history_request_id ON export_history(request_id);
CREATE INDEX IF NOT EXISTS idx_export_history_type ON export_history(export_type);
CREATE INDEX IF NOT EXISTS idx_export_history_status ON export_history(status);
CREATE INDEX IF NOT EXISTS idx_export_history_time ON export_history(time_request);

