.PHONY: help dev dev-hybrid build test docker-up docker-down docker-build clean hound-run hound-batch demo-start demo-reset demo-snapshot \
  stable-up stable-down stable-restart stable-clean \
  backend-up backend-down backend-build backend-restart \
  up down restart all-up all-down all-rebuild ps logs logs-app logs-backend

help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

dev: ## Start all services (SHUTTLE + Chur + verdandi)
	./gradlew devAll

dev-hybrid: ## Keycloak in Docker + Chur + verdandi locally (рекомендуемый режим)
	docker compose up keycloak -d
	@echo "Waiting for Keycloak to be ready..."
	@until docker compose exec keycloak curl -sf http://localhost:9000/health/ready > /dev/null 2>&1; do \
		printf '.'; sleep 3; \
	done
	@echo " Keycloak ready at :18180"
	cd bff/chur && npm run dev &
	cd frontends/verdandi && npm run dev

build: ## Build all projects
	./gradlew :libraries:hound:build :services:shuttle:build :services:heimdall-backend:build :services:dali:build
	cd bff/chur && npm ci && npm run build
	cd frontends/verdandi && npm ci && npm run build

test: ## Run all tests
	./gradlew :libraries:hound:test :services:shuttle:test :services:heimdall-backend:test :services:dali:test
	cd bff/chur && npm test
	cd frontends/verdandi && npm test

docker-build: ## Build all Docker images
	docker compose build

docker-up: ## Start Docker Compose stack (dev)
	docker compose up -d --build

docker-down: ## Stop Docker Compose stack
	docker compose down

hound-run: ## Run Hound CLI (use ARGS= for custom args)
	./gradlew :libraries:hound:run $(if $(ARGS),--args='$(ARGS)',)

hound-batch: ## Run Hound batch processing against local ArcadeDB
	./gradlew :libraries:hound:runBatchLocal

demo-start: ## Поднять полный AIDA demo-стек через Docker
	docker compose up -d
	@sleep 5
	@curl -sf http://localhost:19093/q/health | grep -q UP \
	    && echo "✓ HEIMDALL ready on :19093" || echo "✗ HEIMDALL not yet ready — check: docker compose logs heimdall-backend"
	@curl -sf http://localhost:19090/q/health | grep -q UP \
	    && echo "✓ DALI ready on :19090" || echo "✗ DALI not yet ready — check: docker compose logs dali"
	@echo "✓ Demo stack started"

demo-reset: ## Сбросить demo-состояние (clear HEIMDALL ring buffer + cancel sessions)
	@curl -sf -X POST http://localhost:13000/heimdall/control/reset \
	     -H "Cookie: sid=$$ADMIN_SID" | cat
	@echo ""
	@echo "✓ Demo state reset"

demo-snapshot: ## Сохранить snapshot текущего состояния в FRIGG (name=baseline по умолчанию)
	@curl -sf -X POST "http://localhost:19093/control/snapshot?name=$${SNAPSHOT_NAME:-baseline}" | cat
	@echo ""

clean: ## Clean all build artifacts
	./gradlew clean
	rm -rf bff/chur/node_modules frontends/verdandi/node_modules
	rm -rf bff/chur/dist frontends/verdandi/dist frontends/verdandi/coverage

# ── Layered Docker deployment ─────────────────────────────────────────────────
# Three layers restart independently — never touch stable during normal deploys.
#
#   stable   — Keycloak · FRIGG · Ygg      (stateful infra, image-only)
#   backend  — SHUTTLE · Dali · Heimdall   (Quarkus, rebuild on Java changes)
#   app      — Chur · Verdandi · Heimdall-frontend · Shell · nginx  (fast redeploy)

STABLE  := docker compose -f docker-compose.stable.yml
BACKEND := docker compose -f docker-compose.backend.yml
APP     := docker compose -f docker-compose.yml
_ALL    := docker compose -f docker-compose.stable.yml -f docker-compose.backend.yml -f docker-compose.yml

stable-up: ## Start infrastructure (Keycloak · FRIGG · Ygg)
	$(STABLE) up -d

stable-down: ## Stop infrastructure (data volumes preserved)
	$(STABLE) down

stable-restart: ## Restart infrastructure containers (no rebuild)
	$(STABLE) restart

stable-clean: ## ⚠ DANGER: stop infra and destroy ALL data volumes (KC · FRIGG · Ygg)
	$(STABLE) down -v

backend-up: ## Start backend services (SHUTTLE · Dali · Heimdall-backend)
	$(BACKEND) up -d

backend-down: ## Stop backend services
	$(BACKEND) down

backend-build: ## Build backend images (with layer cache)
	$(BACKEND) build

backend-restart: ## Rebuild backend from scratch and restart  ← use after Java/Quarkus changes
	$(BACKEND) build --no-cache
	$(BACKEND) up -d

up: ## Start app layer (Chur · Verdandi · Heimdall-frontend · Shell · nginx)
	$(APP) up -d

down: ## Stop app layer
	$(APP) down

restart: ## ← MOST COMMON: rebuild app from scratch and restart  (use after Node/React changes)
	$(APP) build --no-cache
	$(APP) up -d

all-up: ## Start all layers in order: stable → backend → app
	$(STABLE) up -d
	@echo "[make] Waiting for FRIGG, Ygg, Keycloak..."
	@until $$(docker inspect --format='{{.State.Health.Status}}' aida-root-keycloak-1 2>/dev/null | grep -q healthy); do printf '.'; sleep 3; done; echo ""
	$(BACKEND) up -d
	$(APP) up -d

all-down: ## Stop all layers gracefully (app → backend → stable)
	-$(APP) down
	-$(BACKEND) down
	$(STABLE) down

all-rebuild: ## Rebuild backend + app images and restart (stable untouched)
	$(BACKEND) build --no-cache
	$(APP) build --no-cache
	$(BACKEND) up -d
	$(APP) up -d

ps: ## Show status of all containers across all layers
	$(_ALL) ps

logs: ## Follow logs from all containers (Ctrl-C to stop)
	$(_ALL) logs -f

logs-app: ## Follow logs from app layer only
	$(APP) logs -f

logs-backend: ## Follow logs from backend layer only
	$(BACKEND) logs -f

log-%: ## Follow logs for a specific service: make log-chur
	$(_ALL) logs -f $*
