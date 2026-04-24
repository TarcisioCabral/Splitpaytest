CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    nfe_key VARCHAR(255) UNIQUE NOT NULL,
    valor_bruto NUMERIC NOT NULL,
    ibs_retido NUMERIC,
    cbs_retido NUMERIC,
    liquido NUMERIC,
    adquirente VARCHAR(255),
    segmento VARCHAR(255),
    fase VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
