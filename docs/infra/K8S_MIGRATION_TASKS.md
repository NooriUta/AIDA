# AIDA — Kubernetes Migration Tasks

**Документ:** `K8S_MIGRATION_TASKS`
**Дата:** 12.04.2026
**Статус:** Backlog · post-HighLoad
**Зависит от:** Docker Compose production работает стабильно

---

## Приоритет 0 — Сделать сейчас, пока пишем код (разблокирует переезд)

### T-K0.1 · Вынести все inter-service URL в env vars
**Что:** Пройтись по всем `application.properties` и `application.yml` — найти и вынести хардкод URL'ов в переменные окружения.

**Затронутые файлы:**
- `services/heimdall-backend/src/main/resources/application.properties` — URL Dali, FRIGG, Chur
- `services/dali/src/main/resources/application.properties` — URL HEIMDALL, YGG
- `services/shuttle/src/main/resources/application.properties` — URL Dali, MIMIR, ANVIL, HEIMDALL
- `bff/chur/src/config.ts` — URL всех backend-сервисов
- `frontends/verdandi/src/.env.example` — URL Chur
- `frontends/heimdall-frontend/src/.env.example` — URL HEIMDALL backend

**Паттерн:**
```properties
# Было (хардкод):
quarkus.rest-client.heimdall-api.url=http://heimdall-backend:9093

# Должно быть (env var):
quarkus.rest-client.heimdall-api.url=${HEIMDALL_URL:http://heimdall-backend:9093}
```

**Критерий готовности:** все URL'ы переопределяются через env без пересборки образа.

---

### T-K0.2 · `.env.k8s.example` ✅ DONE (M2/Prem2)
**Что:** Отдельный файл с k8s-специфичными значениями переменных.

```env
# k8s service discovery (namespace: aida)
SHUTTLE_URL=http://shuttle.aida.svc.cluster.local:8080
HEIMDALL_URL=http://heimdall-backend.aida.svc.cluster.local:9093
DALI_URL=http://dali.aida.svc.cluster.local:9090
CHUR_URL=http://chur.aida.svc.cluster.local:3000
YGG_URL=http://ygg.aida.svc.cluster.local:2480
FRIGG_URL=http://frigg.aida.svc.cluster.local:2481
```

---

## Приоритет 1 — Инфраструктура (post-HighLoad)

### T-K1.1 · Кластер: kind/minikube (dev), k3s или managed cloud (prod)
**Открытый вопрос K-Q1:** какой cloud для первого клиента? Зафиксировать до начала работ.

### T-K1.2 · Container Registry
GitHub Container Registry (GHCR) — бесплатно для публичных, достаточно для начала.

### T-K1.3 · Ingress controller (ingress-nginx)
**Критично для WebSocket** — без этих аннотаций `/heimdall/ws/events` и GraphQL subscriptions сломаются:
```yaml
nginx.ingress.kubernetes.io/proxy-read-timeout: "3600"
nginx.ingress.kubernetes.io/proxy-send-timeout: "3600"
nginx.ingress.kubernetes.io/proxy-http-version: "1.1"
nginx.ingress.kubernetes.io/configuration-snippet: |
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "upgrade";
```

### T-K1.4 · PersistentVolumeClaims
- `ygg-data` — 20Gi (lineage граф, основные данные)
- `frigg-data` — 5Gi (JobRunr state, snapshots)
- `keycloak-data` — 2Gi

---

## Приоритет 2 — Kubernetes manifests (post-HighLoad)

### T-K2.1 · kompose convert из docker-compose
```bash
kompose convert -f docker-compose.yml -o k8s/
```

### T-K2.2 · ConfigMaps + Secrets
ConfigMap `aida-config` — service URLs.
Secret `aida-secrets` — KEYCLOAK_ADMIN_PASSWORD, ARCADEDB_PASSWORD, ANTHROPIC_API_KEY, JWT_SECRET.

### T-K2.3 · Liveness + Readiness probes
Quarkus уже экспортирует `/q/health/live` и `/q/health/ready`.

### T-K2.4 · Keycloak StatefulSet + realm import
Realm export `keycloak/seer-realm.json` уже есть в репо — проверить актуальность.

### T-K2.5 · Ingress rules
Path routing: `/verdandi` → verdandi:5173, `/heimdall` → heimdall-frontend:5174, `/` → chur:3000.

---

## Приоритет 3 — OrchestratorPort k8s-адаптер (post-HighLoad)

### T-K3.1 · ServiceAccount с минимальным RBAC
Права: `patch deployments` (rolling restart) + `delete pods`.

### T-K3.2 · K8sOrchestratorAdapter.java
```java
// fabric8 kubernetes-client:6.x
@ApplicationScoped @Named("kubernetes")
public class K8sOrchestratorAdapter implements OrchestratorPort {
    @Inject KubernetesClient client;

    @Override public void restart(String svc) {
        client.apps().deployments().inNamespace("aida")
            .withName(toDeploymentName(svc)).rolling().restart();
    }
    @Override public void stop(String svc) {
        client.apps().deployments().inNamespace("aida")
            .withName(toDeploymentName(svc)).scale(0);
    }
}
```

---

## Приоритет 4 — CI/CD (post-HighLoad)

### T-K4.1 · GitHub Actions: build → push GHCR → kubectl apply
### T-K4.2 · Helm chart (nice-to-have, после первого деплоя)

---

## Открытые вопросы

| # | Вопрос | Срок |
|---|---|---|
| K-Q1 | Какой кластер для prod? (k3s на VM / managed cloud) | перед началом работ |
| K-Q2 | Домен для prod? TLS через cert-manager? | T-K1.3 done |
| K-Q3 | kube-prometheus-stack сразу или потом? | post-HighLoad |
| K-Q4 | Helm или голые manifests? | после первого деплоя |
| K-Q5 | Multi-node или single-node для первого клиента? | K-Q1 done |

---

## Критический путь

```
T-K0.1 (env vars) ← СЕЙЧАС, пока пишем код
T-K0.2 (.env.k8s) ✅ DONE

    ▼ post-HighLoad
T-K1.x → T-K2.x → T-K3.2 → T-K4.1 → 🎯 first k8s deploy
```

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.1 | T-K0.2 .env.k8s.example done в M2/Prem2. T-K0.1 env vars — часть сделана (HEIMDALL_URL в docker-compose). |
| 12.04.2026 | 1.0 | Initial. T-K0.1/T-K0.2 — сейчас. Всё остальное post-HighLoad. |
