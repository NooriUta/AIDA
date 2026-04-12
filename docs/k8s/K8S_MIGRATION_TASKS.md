# AIDA — Kubernetes Migration Tasks

**Документ:** `K8S_MIGRATION_TASKS`
**Дата:** 12.04.2026
**Статус:** Backlog · post-HighLoad
**Зависит от:** Docker Compose production работает стабильно

---

## Приоритет 0 — Сделать сейчас, пока пишем код (разблокирует переезд)

### T-K0.1 · Вынести все inter-service URL в env vars ✅
**Что:** Пройтись по всем `application.properties` и `application.yml` — найти и вынести хардкод URL'ов в переменные окружения.

**Затронутые файлы:**
- `services/heimdall-backend/src/main/resources/application.properties` — FRIGG_URL, CORS_ORIGINS ✅
- `services/shuttle/src/main/resources/application.properties` — YGG_URL, HEIMDALL_URL, ARCADEDB_* ✅
- `bff/chur/src/config.ts` — уже на env vars; добавлен `.env.example` ✅
- `frontends/heimdall-frontend/.env.example` — VITE_HEIMDALL_API, VITE_HEIMDALL_WS ✅
- `frontends/verdandi/.env.example` — SHUTTLE_URL, CHUR_URL ✅

**Паттерн (Quarkus):**
```properties
# Было (хардкод):
quarkus.rest-client.heimdall-api.url=http://heimdall-backend:9093

# Должно быть (env var):
quarkus.rest-client.heimdall-api.url=${HEIMDALL_URL:http://localhost:9093}
```

**Критерий готовности:** все URL'ы переопределяются через env без пересборки образа.

---

### T-K0.2 · Добавить `.env.k8s.example` рядом с `.env.example` ✅

Файлы созданы:
- `bff/chur/.env.k8s.example`
- `frontends/heimdall-frontend/.env.k8s.example`
- `frontends/verdandi/.env.k8s.example`

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

## Приоритет 1 — Инфраструктура (нужна до первого деплоя в k8s)

### T-K1.1 · Выбрать и поднять кластер
**Варианты:**
- Dev: `kind` (Kubernetes in Docker) или `minikube` — для локальной разработки
- Prod: k3s на VM (дешевле), managed GKE/EKS/AKS (проще ops)

**Открытый вопрос:** какой cloud/хостинг для первого клиента? Зафиксировать до начала работ.

---

### T-K1.2 · Настроить Container Registry
**Варианты:** GitHub Container Registry (GHCR) — бесплатно для публичных, дёшево для приватных. Достаточно для начала.

**Что нужно:**
- Создать организацию/repo в GHCR
- Добавить в CI/CD push образов после сборки
- Обновить Dockerfiles чтобы тегировались версией + `latest`

---

### T-K1.3 · Настроить Ingress controller
**Рекомендация:** `ingress-nginx` — стандарт, хорошая документация.

**Конфигурация для WebSocket (критично для HEIMDALL):**
```yaml
nginx.ingress.kubernetes.io/proxy-read-timeout: "3600"
nginx.ingress.kubernetes.io/proxy-send-timeout: "3600"
nginx.ingress.kubernetes.io/proxy-http-version: "1.1"
nginx.ingress.kubernetes.io/configuration-snippet: |
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "upgrade";
```
Без этих аннотаций WebSocket `/heimdall/ws/events` и GraphQL subscriptions сломаются.

---

### T-K1.4 · PersistentVolumeClaims для stateful сервисов
**Критично:** без PVC данные теряются при рестарте pod'а.

```yaml
# YGG (HoundArcade) — lineage граф, основные данные
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: ygg-data
  namespace: aida
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 20Gi
---
# FRIGG — JobRunr state, snapshots, user prefs
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: frigg-data
  namespace: aida
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 5Gi
---
# Keycloak — realm config, users
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: keycloak-data
  namespace: aida
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 2Gi
```

---

## Приоритет 2 — Kubernetes manifests

### T-K2.1 · Сгенерировать базовые manifests из docker-compose
```bash
brew install kompose
kompose convert -f docker-compose.yml -o k8s/
# Результат: Deployment + Service для каждого сервиса
# Потребуется ручная правка (см. T-K2.2 — T-K2.5)
```

---

### T-K2.2 · ConfigMaps и Secrets
**ConfigMap** (`aida-config`):
```yaml
data:
  SHUTTLE_URL: "http://shuttle.aida.svc.cluster.local:8080"
  HEIMDALL_URL: "http://heimdall-backend.aida.svc.cluster.local:9093"
  DALI_URL: "http://dali.aida.svc.cluster.local:9090"
  YGG_URL: "http://ygg.aida.svc.cluster.local:2480"
  FRIGG_URL: "http://frigg.aida.svc.cluster.local:2481"
  KEYCLOAK_URL: "http://keycloak.aida.svc.cluster.local:8080"
```

**Secret** (`aida-secrets`):
```yaml
# Base64-encoded:
KEYCLOAK_ADMIN_PASSWORD: ...
ARCADEDB_PASSWORD: ...
ANTHROPIC_API_KEY: ...
JWT_SECRET: ...
```

---

### T-K2.3 · Liveness и Readiness probes
Quarkus экспортирует `/q/health/live` и `/q/health/ready`:
```yaml
livenessProbe:
  httpGet:
    path: /q/health/live
    port: 9093
  initialDelaySeconds: 30
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /q/health/ready
    port: 9093
  initialDelaySeconds: 15
  periodSeconds: 5
```
Для Chur (Node.js) — добавить `GET /health` endpoint.

---

### T-K2.4 · Keycloak в k8s
- StatefulSet (не Deployment) — для стабильного hostname
- PVC (см. T-K1.4)
- Realm export: `keycloak/seer-realm.json` — проверить актуальность
- Init container или Job для автоматического импорта realm

---

### T-K2.5 · Ingress rules для всех сервисов
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: aida-ingress
  namespace: aida
  annotations:
    nginx.ingress.kubernetes.io/proxy-read-timeout: "3600"
    nginx.ingress.kubernetes.io/proxy-send-timeout: "3600"
spec:
  rules:
  - host: seer.yourdomain.com
    http:
      paths:
      - path: /verdandi
        pathType: Prefix
        backend:
          service: {name: verdandi, port: {number: 5173}}
      - path: /heimdall
        pathType: Prefix
        backend:
          service: {name: heimdall-frontend, port: {number: 5174}}
      - path: /
        pathType: Prefix
        backend:
          service: {name: chur, port: {number: 3000}}
```

---

## Приоритет 3 — OrchestratorPort k8s-адаптер (HEIMDALL)

### T-K3.1 · ServiceAccount с минимальным RBAC
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: heimdall-orchestrator
  namespace: aida
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: heimdall-orchestrator-role
  namespace: aida
rules:
- apiGroups: ["apps"]
  resources: ["deployments"]
  verbs: ["get", "list", "patch"]
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list", "delete"]
```

---

### T-K3.2 · K8sOrchestratorAdapter.java
```java
// Зависимость: io.fabric8:kubernetes-client:6.x
@ApplicationScoped
@Named("kubernetes")
public class K8sOrchestratorAdapter implements OrchestratorPort {

    @Inject KubernetesClient client;

    // Restart = rolling update (zero downtime)
    @Override
    public void restart(String serviceName) {
        client.apps().deployments()
            .inNamespace("aida")
            .withName(toDeploymentName(serviceName))
            .rolling().restart();
    }

    // Stop = scale to 0
    @Override
    public void stop(String serviceName) {
        client.apps().deployments()
            .inNamespace("aida")
            .withName(toDeploymentName(serviceName))
            .scale(0);
    }
}
```

> **Примечание (multiple instances):** при scale > 1 `rolling().restart()` гарантирует zero downtime — k8s заменяет поды по одному. `stop()` (scale=0) остановит все инстансы. Для частичного масштабирования: `scale(currentReplicas - 1)`.

---

## Приоритет 4 — CI/CD

### T-K4.1 · GitHub Actions: build → push → deploy
```yaml
# .github/workflows/deploy.yml
jobs:
  build-and-push:
    # Build Docker images, push to GHCR
  deploy:
    # kubectl apply -f k8s/ --namespace aida
    # kubectl rollout status deployment/shuttle -n aida
```

---

### T-K4.2 · Helm chart (опционально, nice-to-have)
Позволяет одной командой деплоить в dev/staging/prod с разными values. Решить после первого успешного деплоя.

---

## Открытые вопросы

| # | Вопрос | Влияет на | Срок |
|---|---|---|---|
| K-Q1 | Какой кластер для prod? (k3s на VM / managed cloud) | T-K1.1 | перед началом работ |
| K-Q2 | Домен для prod? TLS через cert-manager? | T-K2.5 | T-K1.3 done |
| K-Q3 | Мониторинг: kube-prometheus-stack сразу или потом? | T-K4.x | post-HighLoad |
| K-Q4 | Helm или голые manifests? | T-K4.2 | после первого деплоя |
| K-Q5 | Multi-node или single-node кластер для первого клиента? | T-K1.1 | K-Q1 done |

---

## Критический путь

```
T-K0.1 (env vars) ✅ сделано 12.04.2026
T-K0.2 (.env.k8s) ✅ сделано 12.04.2026
    │
    ▼ post-HighLoad
T-K1.1 (кластер)  ← зависит от K-Q1
T-K1.2 (registry) ←─┐
T-K1.3 (ingress)  ←─┤ параллельно
T-K1.4 (PVC)      ←─┘
    │
T-K2.1 (kompose)
T-K2.2 (secrets)  ←─┐
T-K2.3 (probes)   ←─┤ параллельно
T-K2.4 (keycloak) ←─┘
T-K2.5 (ingress rules)
    │
T-K3.2 (K8sOrchestratorAdapter)
T-K4.1 (CI/CD)
    │
🎯 First k8s deploy
```

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.0 | Initial. По итогам обсуждения архитектуры HEIMDALL OrchestratorPort + k8s migration blockers. |
| 12.04.2026 | 1.1 | T-K0.1 и T-K0.2 выполнены. Добавлена заметка про multiple instances в T-K3.2. |
