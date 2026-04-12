.PHONY: help dev build test docker-up docker-down docker-build clean hound-run hound-batch

help: ## Show available targets
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

dev: ## Start all services (SHUTTLE + Chur + verdandi)
	./gradlew devAll

build: ## Build all projects
	./gradlew :libraries:hound:build :services:shuttle:build
	cd bff/chur && npm ci && npm run build
	cd frontends/verdandi && npm ci && npm run build

test: ## Run all tests
	./gradlew :libraries:hound:test :services:shuttle:test
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

clean: ## Clean all build artifacts
	./gradlew clean
	rm -rf bff/chur/node_modules frontends/verdandi/node_modules
	rm -rf bff/chur/dist frontends/verdandi/dist frontends/verdandi/coverage
