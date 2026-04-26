-- =============================================================
-- V2 — Seed data for development / demo purposes
-- Dados de exemplo para o dashboard de transações SplitPay IVA
-- =============================================================
-- Segmentos: VAREJO, SERVICOS, INDUSTRIA, AGRO
-- Fases: 2026, 2027, 2028
-- Adquirentes: Cielo, Stone, Rede, PagSeguro, GetNet
-- IBS  ≈ 0.5 % do valor bruto por fase 2026 (crescente nos anos seguintes)
-- CBS  ≈ 0.5 % do valor bruto por fase 2026
-- =============================================================

INSERT INTO transactions (nfe_key, valor_bruto, ibs_retido, cbs_retido, liquido, adquirente, segmento, fase, created_at)
VALUES
    -- Fase 2026 — VAREJO
    ('35260312000199000101550010000012341234567890',  1850.00,   9.25,   9.25,  1831.50, 'Cielo',      'VAREJO',    '2026', NOW() - INTERVAL '2 hours'),
    ('35260312000199000101550010000023452345678901',  3420.50,  17.10,  17.10,  3386.30, 'Stone',      'VAREJO',    '2026', NOW() - INTERVAL '4 hours'),
    ('35260312000199000101550010000034563456789012',   750.00,   3.75,   3.75,   742.50, 'Rede',       'VAREJO',    '2026', NOW() - INTERVAL '6 hours'),

    -- Fase 2026 — SERVIÇOS
    ('35260312000199000101550010000045674567890123',  5200.00,  26.00,  26.00,  5148.00, 'PagSeguro',  'SERVICOS',  '2026', NOW() - INTERVAL '8 hours'),
    ('35260312000199000101550010000056785678901234',   980.75,   4.90,   4.90,   970.95, 'GetNet',     'SERVICOS',  '2026', NOW() - INTERVAL '10 hours'),

    -- Fase 2026 — INDÚSTRIA
    ('35260312000199000101550010000067896789012345', 12400.00,  62.00,  62.00, 12276.00, 'Cielo',      'INDUSTRIA', '2026', NOW() - INTERVAL '12 hours'),
    ('35260312000199000101550010000078907890123456',  2100.00,  10.50,  10.50,  2079.00, 'Stone',      'INDUSTRIA', '2026', NOW() - INTERVAL '14 hours'),

    -- Fase 2027 — VAREJO (alíquota maior)
    ('35270312000199000101550010000089018901234567',  4300.00,  43.00,  43.00,  4214.00, 'Rede',       'VAREJO',    '2027', NOW() - INTERVAL '1 day'),
    ('35270312000199000101550010000090129012345678',   620.90,   6.21,   6.21,   608.48, 'PagSeguro',  'VAREJO',    '2027', NOW() - INTERVAL '1 day 2 hours'),

    -- Fase 2027 — AGRO
    ('35270312000199000101550010000001230123456789',  8750.00,  87.50,  87.50,  8575.00, 'GetNet',     'AGRO',      '2027', NOW() - INTERVAL '2 days'),

    -- Fase 2028 — SERVIÇOS (alíquota plena)
    ('35280312000199000101550010000012341234567891', 15000.00, 225.00, 225.00, 14550.00, 'Cielo',      'SERVICOS',  '2028', NOW() - INTERVAL '3 days'),
    ('35280312000199000101550010000023452345678902',  3300.00,  49.50,  49.50,  3201.00, 'Stone',      'INDUSTRIA', '2028', NOW() - INTERVAL '4 days')

ON CONFLICT (nfe_key) DO NOTHING;
