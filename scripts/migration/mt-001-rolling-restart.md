# DMT-12: Rolling Restart Test Runbook

## Goal
Verify that 2 Dali workers can handle a rolling restart mid-job with zero job loss.

## Setup

```bash
# Start 2 Dali replicas behind a load balancer
docker compose up --scale dali=2

# Enqueue 10 parse jobs (adapt source path to your environment)
for i in $(seq 1 10); do
  curl -s -X POST http://localhost:9090/api/sessions \
    -H "Content-Type: application/json" \
    -H "X-Seer-Tenant-Alias: default" \
    -d "{\"dialect\":\"plsql\",\"source\":\"/path/to/test.sql\",\"preview\":true}"
done
```

## Rolling Restart Procedure

```bash
# While jobs are running, restart replica 1
docker compose restart dali_1

# Wait for it to come back up and rejoin the cluster
sleep 30

# Restart replica 2
docker compose restart dali_2
```

## Assertions

After all restarts:
1. All 10 sessions in GET /api/sessions should be COMPLETED or FAILED (never stuck in QUEUED/RUNNING)
2. No duplicate job execution (each session appears exactly once)
3. Sessions that were QUEUED/RUNNING during restart are marked FAILED on startup (SessionService.onStart behavior)

```bash
# Check session states
curl -s http://localhost:9090/api/sessions \
  -H "X-Seer-Tenant-Alias: default" \
  | jq '[.[] | {id, status}]'
```

## Expected Behavior

- Sessions that were RUNNING during restart → marked FAILED on restart (per SessionService.onStart recovery logic)
- Sessions that were QUEUED → reprocessed by JobRunr (job state persisted in FRIGG)
- New sessions submitted after restart → processed normally

## JobRunr Recovery

JobRunr OSS 8.5.2 persists job state in ArcadeDB (via `ArcadeDbStorageProvider`).
On restart, the `BackgroundJobServer` picks up interrupted jobs and retries them
according to the `@Job(retries=3)` annotation on `ParseJob.execute()`.

## Success Criteria

- 0 sessions permanently stuck in QUEUED or RUNNING after restarts complete
- 0 sessions in FAILED state due to a bug (FAILED only from actual parse errors or restart interruption)
- P99 latency for new sessions < 2s post-restart
