-- Initial schema for conciliation service
CREATE TABLE IF NOT EXISTS conciliations (
    id          BIGSERIAL PRIMARY KEY,
    nfe_key     VARCHAR(255) NOT NULL UNIQUE,
    valor_bruto NUMERIC(19, 2) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    message     TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
