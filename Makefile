.PHONY: help dev dev-hybrid build test docker-up docker-down docker-build clean hound-run hound-batch demo-start demo-reset demo-snapshot

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
