ALTER TABLE transactions ADD COLUMN currency VARCHAR(10) DEFAULT 'BRL';
ALTER TABLE transactions ADD COLUMN original_amount DECIMAL(19,4);
ALTER TABLE transactions ADD COLUMN exchange_rate DECIMAL(19,4) DEFAULT 1.0;
