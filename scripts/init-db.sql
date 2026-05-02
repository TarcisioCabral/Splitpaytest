SELECT 'CREATE DATABASE splitpay_transaction' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'splitpay_transaction')\gexec
SELECT 'CREATE DATABASE splitpay_conciliation' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'splitpay_conciliation')\gexec
SELECT 'CREATE DATABASE splitpay_reports' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'splitpay_reports')\gexec
SELECT 'CREATE DATABASE splitpay_auth' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'splitpay_auth')\gexec
