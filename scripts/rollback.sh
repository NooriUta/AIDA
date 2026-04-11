#!/usr/bin/env bash
# rollback.sh — откатить стек на конкретный SHA
#
# Использование:
#   ./scripts/rollback.sh <sha>
#
# Пример:
#   ./scripts/rollback.sh abc1234f
#
# Требования: docker, docker compose, доступ к ghcr.io
#
set -euo pipefail

SHA=${1:?Usage: rollback.sh <git-sha>}
REPO="ghcr.io/nooriuta/verdandi"
COMPOSE_DIR="/opt/seer-studio"
COMPOSE_FILE="${COMPOSE_DIR}/docker-compose.prod.yml"

echo "Rolling back to ${SHA}..."

# Проверить что образы с таким тегом существуют в GHCR
for svc in verdandi chur shuttle; do
  echo "  Pulling ${REPO}/${svc}:${SHA}"
  docker pull "${REPO}/${svc}:${SHA}"
done

# Пометить как latest
for svc in verdandi chur shuttle; do
  docker tag "${REPO}/${svc}:${SHA}" "${REPO}/${svc}:latest"
done

# Перезапустить стек
cd "${COMPOSE_DIR}"
docker compose -f "${COMPOSE_FILE}" up -d --remove-orphans

# Подождать
sleep 15

# Health-check
echo "Running health checks..."
curl -sf http://localhost:13000/health  || { echo "FAIL: Chur";    exit 1; }
curl -sf http://localhost:18080/q/health || { echo "FAIL: SHUTTLE"; exit 1; }
curl -sf http://localhost:15173          || { echo "FAIL: verdandi"; exit 1; }

echo "Rollback to ${SHA} successful."
