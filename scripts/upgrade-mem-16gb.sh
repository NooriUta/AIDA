#!/bin/bash
# Запускать после апгрейда VM до 16 GB.
# Обновляет лимиты памяти в docker-compose.yc.yml и перезапускает контейнеры.

set -e

COMPOSE_DIR="/opt/seer-studio"
COMPOSE_FILE="$COMPOSE_DIR/docker-compose.yc.yml"

echo "=== Backup ==="
cp "$COMPOSE_FILE" "$COMPOSE_FILE.bak.$(date +%Y%m%d_%H%M%S)"
echo "Saved to $COMPOSE_FILE.bak.*"

echo ""
echo "=== Updating memory limits ==="
python3 << 'PYEOF'
import re

path = '/opt/seer-studio/docker-compose.yc.yml'

# New limits for 16 GB VM
# Total: ~9.5 GB limits  →  6.5 GB headroom
new_limits = {
    'keycloak': {'limit': '1G',    'reservation': '512M', 'cpus': '2.0'},
    'dali':     {'limit': '1536M', 'reservation': '768M', 'cpus': '2.0'},
    'frigg':    {'limit': '1536M', 'reservation': '768M', 'cpus': '1.0'},
    'ygg':      {'limit': '4G',    'reservation': '2G',   'cpus': '2.0'},
}

with open(path) as f:
    lines = f.readlines()

current_service = None
in_limits = False
in_reservations = False
result = []

for line in lines:
    stripped = line.strip()

    # Detect top-level service block (exactly 2-space indent)
    svc_match = re.match(r'^  ([a-zA-Z][a-zA-Z0-9_-]*):\s*$', line)
    if svc_match:
        current_service = svc_match.group(1)
        in_limits = False
        in_reservations = False

    if current_service in new_limits:
        cfg = new_limits[current_service]

        if stripped == 'limits:':
            in_limits, in_reservations = True, False
        elif stripped == 'reservations:':
            in_limits, in_reservations = False, True
        elif stripped.startswith('memory:') and in_limits:
            line = re.sub(r'memory: \S+', f"memory: {cfg['limit']}", line)
        elif stripped.startswith('memory:') and in_reservations:
            line = re.sub(r'memory: \S+', f"memory: {cfg['reservation']}", line)
        elif stripped.startswith("cpus:") and in_limits and 'cpus' in cfg:
            line = re.sub(r"cpus: '[^']*'", f"cpus: '{cfg['cpus']}'", line)

    result.append(line)

with open(path, 'w') as f:
    f.writelines(result)

print("Done.")
PYEOF

echo ""
echo "=== New limits preview ==="
grep -E "memory:|cpus:" "$COMPOSE_FILE" | grep -v "#"

echo ""
echo "=== Recreating containers ==="
cd "$COMPOSE_DIR"
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml up -d --force-recreate \
    dali frigg ygg keycloak

echo ""
echo "=== Waiting 40s for JVM warm-up ==="
sleep 40

echo ""
echo "=== Memory after upgrade ==="
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.CPUPerc}}"
