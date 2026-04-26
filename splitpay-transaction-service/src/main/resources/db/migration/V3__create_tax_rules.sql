CREATE TABLE tax_rules (
    id BIGSERIAL PRIMARY KEY,
    segmento VARCHAR(50) NOT NULL,
    fase VARCHAR(50) NOT NULL,
    ibs_rate DECIMAL(10,4) NOT NULL,
    cbs_rate DECIMAL(10,4) NOT NULL,
    UNIQUE(segmento, fase)
);

-- Seed default rules (based on current hardcoded logic)
-- 2026_teste
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('geral', '2026_teste', 0.005, 0.005);
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('alimentacao', '2026_teste', 0.002, 0.002);
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('saude', '2026_teste', 0.002, 0.002);
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('educacao', '2026_teste', 0.0015, 0.0015);

-- 2027_cbs
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('geral', '2027_cbs', 0.00, 0.135);
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('alimentacao', '2027_cbs', 0.00, 0.054);
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('saude', '2027_cbs', 0.00, 0.054);
INSERT INTO tax_rules (segmento, fase, ibs_rate, cbs_rate) VALUES ('educacao', '2027_cbs', 0.00, 0.0405);
