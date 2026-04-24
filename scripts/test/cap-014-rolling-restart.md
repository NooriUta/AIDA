# CAP-14: Chur Rolling Restart Test Runbook

## Goal
Verify 2 Chur replicas behind a load balancer sustain 100 active sessions
through a rolling restart with 0 logouts and P99 latency ≤ 100ms.

## Setup

```bash
# Start 2 Chur replicas + FRIGG + Keycloak
docker compose up --scale chur=2 -d

# Create 100 sessions (load gen)
for i in $(seq 1 100); do
  curl -s -X POST http://localhost:3000/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}' \
    -c "/tmp/session-${i}.jar" &
done
wait

# Verify 100 sessions in ArcadeDB
curl -s -X POST "http://localhost:2481/api/v1/command/frigg-sessions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)" \
  -d '{"language":"sql","command":"SELECT count() FROM DaliChurSession"}' \
  | jq '.result[0].count'
```

## Rolling Restart Procedure

```bash
# While sessions are active, restart replica 1
docker compose restart chur_1

# Wait for replica 1 to come back
sleep 15

# Restart replica 2
docker compose restart chur_2

# Wait for replica 2 to come back
sleep 15
```

## During Restart — Latency Monitoring

```bash
# In a separate terminal, poll /health every second to measure latency
while true; do
  start=$(date +%s%N)
  curl -s http://localhost:3000/health > /dev/null
  end=$(date +%s%N)
  echo "$(( (end - start) / 1000000 ))ms"
  sleep 1
done
```

## Assertions

After all restarts complete:

```bash
# 1. All 100 sessions still valid (no logouts)
for i in $(seq 1 100); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:3000/auth/me \
    -b "/tmp/session-${i}.jar")
  [ "$code" != "200" ] && echo "LOGOUT: session $i ($code)"
done

# 2. Session count unchanged in ArcadeDB
curl -s -X POST "http://localhost:2481/api/v1/command/frigg-sessions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)" \
  -d '{"language":"sql","command":"SELECT count() FROM DaliChurSession"}' \
  | jq '.result[0].count'
```

## Success Criteria

- 0 sessions lost (no 401 on `/auth/me` post-restart)
- P99 latency for `/health` ≤ 100ms during restart
- Cache hit rate ≥ 90% (verify via `CachedSessionStore` LRU — most reads from in-memory)
- ArcadeDB `DaliChurSession` count unchanged

## Chaos Test

```bash
# Kill replica 1 mid-request (not graceful restart)
docker compose kill chur_1

# Immediately verify sessions still work via replica 2
for i in $(seq 1 20); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:3000/auth/me \
    -b "/tmp/session-${i}.jar")
  echo "session $i: $code"
done

# Bring replica 1 back
docker compose up -d chur
```
