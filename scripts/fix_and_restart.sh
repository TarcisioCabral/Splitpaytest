#!/usr/bin/env bash
# ============================================================
# fix_and_restart.sh
# Resolve o bug de "permission denied" ao parar containers
# que foram iniciados com sudo, e reinicia a stack completa.
# 
# USO:  sudo bash scripts/fix_and_restart.sh
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║       Splitpay — Fix & Restart (requer sudo)        ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

# 1. Para e remove TODOS os containers do projeto (com permissão root)
echo "► Parando todos os containers do projeto..."
docker ps -a --filter "name=splitpay" --format "{{.ID}}" | xargs -r docker stop --time 5 2>/dev/null || true
docker ps -a --filter "name=splitpay" --format "{{.ID}}" | xargs -r docker rm -f 2>/dev/null || true

# 2. Remove containers órfãos com hash no nome (ex: c545cfc_splitpay-*)
echo "► Removendo containers órfãos..."
docker ps -a --format "{{.Names}}" | grep -E '^[a-f0-9]{12}_splitpay' | xargs -r docker rm -f 2>/dev/null || true
docker ps -a --filter "name=comercio-mysql" --format "{{.ID}}" | xargs -r docker rm -f 2>/dev/null || true

echo ""
echo "► Status após limpeza:"
docker ps -a --format "table {{.Names}}\t{{.Status}}" | grep -E "splitpay|NAME" || echo "(nenhum container splitpay)"

echo ""
echo "► Subindo a stack completa..."
docker compose up -d

echo ""
echo "► Aguardando serviços ficarem saudáveis (60s)..."
sleep 60

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║                   STATUS FINAL                      ║"
echo "╚══════════════════════════════════════════════════════╝"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
