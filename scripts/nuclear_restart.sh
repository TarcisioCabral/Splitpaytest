#!/usr/bin/env bash
# ============================================================
# nuclear_restart.sh  —  DEVE ser rodado com: sudo bash scripts/nuclear_restart.sh
# Reinicia o daemon Docker para liberar containers travados
# e sobe toda a stack limpa.
# ============================================================
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║   Splitpay — Reinício Total do Docker Daemon        ║"
echo "╚══════════════════════════════════════════════════════╝"

# 1. Para o daemon Docker (força kill em TODOS os containers, sem exceção)
echo ""
echo "► [1/4] Reiniciando o serviço Docker..."
systemctl restart docker
sleep 5

echo "► Docker daemon reiniciado. Todos os containers foram encerrados."

# 2. Confirma que não há nada rodando
RUNNING=$(docker ps -q | wc -l)
echo "► Containers rodando agora: $RUNNING (esperado: 0)"

# 3. Reconstrói apenas o conciliation-service (teve SQL corrigido)
echo ""
echo "► [2/4] Reconstruindo imagem do conciliation-service (SQL corrigido)..."
docker compose build conciliation-service

# 4. Sobe a stack completa
echo ""
echo "► [3/4] Subindo a stack completa..."
docker compose up -d

# 5. Aguarda e mostra status
echo ""
echo "► [4/4] Aguardando serviços (90s)..."
sleep 90

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║                   STATUS FINAL                      ║"
echo "╚══════════════════════════════════════════════════════╝"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Verifica saúde dos serviços críticos
echo ""
echo "► Verificando endpoints de saúde..."
for svc in "transaction-service:9081" "auth-service:9084" "conciliation-service:9082" "api-gateway:9085"; do
  name="${svc%%:*}"
  port="${svc##*:}"
  status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/actuator/health" 2>/dev/null || echo "ERR")
  if [ "$status" = "200" ]; then
    echo "  ✅ $name → HTTP $status"
  else
    echo "  ❌ $name → HTTP $status (pode ainda estar iniciando)"
  fi
done
echo ""
