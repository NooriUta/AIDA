# AIDA / SEER Studio — Documentation

Public documentation for contributors and users of the SEER Studio platform.

---

## Guides

Practical how-to guides for installation, development, and deployment.

| Document | Description |
|---|---|
| [INSTALLATION.md](guides/INSTALLATION.md) | First-time server setup: Docker, Nginx, Certbot, DNS |
| [DEVELOPMENT.md](guides/DEVELOPMENT.md) | Local dev environment, services, commands |
| [YC_DEPLOYMENT.md](guides/YC_DEPLOYMENT.md) | Production deployment on Yandex Cloud |
| [STARTUP_SEQUENCE.md](guides/STARTUP_SEQUENCE.md) | Service startup order, health checks, known issues |
| [CICD_SETUP.md](guides/CICD_SETUP.md) | GitHub Actions secrets, registries, rollback |
| [BRANCHING_STRATEGY.md](guides/BRANCHING_STRATEGY.md) | Gitflow model, release process, commit format |
| [PORT_MAPPING.md](guides/PORT_MAPPING.md) | Dev and Docker port reference |
| [FRIGG.md](guides/FRIGG.md) | FRIGG (ArcadeDB state store) setup and usage |
| [ONBOARDING.md](guides/ONBOARDING.md) | New team member checklist |

## Architecture

High-level system architecture and service overview.

| Document | Description |
|---|---|
| [OVERVIEW.md](architecture/OVERVIEW.md) | System overview, data flow, component map |
| [dali-overview.md](architecture/dali-overview.md) | DALI service capabilities and modes |
| [diagrams/](diagrams/README.md) | C4 diagrams, data layer, deployment topology |

## Reference

API and schema reference.

| Document | Description |
|---|---|
| [SHUTTLE_QUERY_REFERENCE.md](reference/SHUTTLE_QUERY_REFERENCE.md) | GraphQL query examples |
| [YGG_SCHEMA_REFERENCE.md](reference/YGG_SCHEMA_REFERENCE.md) | ArcadeDB lineage graph schema |
| [HOUND_POSTGRESQL_SPEC.md](reference/HOUND_POSTGRESQL_SPEC.md) | Supported PostgreSQL SQL dialect |

---

> Internal team documentation (sprint plans, ADRs, specs, reviews) lives in `C:\AIDA\docs\`.
