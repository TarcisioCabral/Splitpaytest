CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255),
    username VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    details TEXT
);
