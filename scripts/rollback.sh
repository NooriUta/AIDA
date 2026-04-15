#!/usr/bin/env bash
# rollback.sh — откатить стек на конкретный SHA
#
# Использование:
#   ./scripts/rollback.sh <sha>
#
# Пример:
#   ./scripts/rollback.sh abc1234f
#
# Требования: docker, docker compose, yc CLI, доступ к YCR / GHCR
#
set -euo pipefail

SHA=${1:?Usage: rollback.sh <git-sha>}
COMPOSE_DIR="/opt/seer-studio"

# YCR (primary) — быстрее в пределах YC-региона
YCR_REPO="cr.yandex/${YC_REGISTRY_ID:?YC_REGISTRY_ID not set}"
# GHCR (fallback) — если YCR недоступен
GHCR_REPO="ghcr.io/nooriuta/verdandi"

echo "Rolling back to ${SHA}..."

# Пробуем YCR, fallback на GHCR
for svc in verdandi chur shuttle; do
  if docker pull "${YCR_REPO}/${svc}:${SHA}" 2>/dev/null; then
    docker tag "${YCR_REPO}/${svc}:${SHA}" "${YCR_REPO}/${svc}:latest"
    echo "  [${svc}] pulled from YCR"
  else
    echo "  [${svc}] YCR miss — falling back to GHCR"
    docker pull "${GHCR_REPO}/${svc}:${SHA}"
    docker tag  "${GHCR_REPO}/${svc}:${SHA}" "${YCR_REPO}/${svc}:latest"
  fi
done

# Перезапустить стек
cd "${COMPOSE_DIR}"
docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml \
  up -d --remove-orphans

# Health checks с retry (K-8 паттерн)
echo "Waiting for services..."
for svc in \
  "http://localhost:13000/health:Chur" \
  "http://localhost:18080/q/health:SHUTTLE" \
  "http://localhost:15173/:verdandi"; do
  url="${svc%%:*}"
  name="${svc##*:}"
  for i in $(seq 1 10); do
    curl -sf "${url}" && break
    echo "  [${name}] retry ${i}/10..."; sleep 5
  done || { echo "${name} health check FAILED"; exit 1; }
  echo "  [${name}] OK"
done

echo "Rollback to ${SHA} successful."
