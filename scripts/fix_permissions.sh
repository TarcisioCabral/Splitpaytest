#!/usr/bin/env bash
# Fix: mata containers travados com permissão root e reinicia a stack

set -e

echo "==> Matando processos dos containers travados (requer sudo)..."
sudo kill -9 9018 8971 2>/dev/null || true

echo "==> Aguardando Docker detectar os containers mortos..."
sleep 3

echo "==> Removendo containers parados..."
docker rm -f splitpay-auth-service splitpay-transaction-service 2>/dev/null || true

echo "==> Subindo toda a stack..."
docker compose up -d

echo ""
echo "==> Status final:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
