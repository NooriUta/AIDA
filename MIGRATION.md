# Migration: VERDANDI → aida-root monorepo

## Date: 2026-04-12
## Reason
Consolidation of Hound (SQL parser/semantic engine) and VERDANDI stack
(SHUTTLE + Chur + verdandi) into a single monorepo for unified builds,
shared dependency management, and preparation for new services (Dali, Mimir, etc.)

## What changed

### Repository
- Old repo: github.com/NooriUta/Verdandi (archived)
- New repo: github.com/NooriUta/aida
- VERDANDI git history preserved via `git merge --allow-unrelated-histories`
- Hound added as fresh commit (no prior git history)

### Directory mapping (old → new)

| Old path                          | New path                  |
|-----------------------------------|---------------------------|
| Dali4/HOUND/Hound/               | libraries/hound/          |
| SEERStudio/VERDANDI/SHUTTLE/      | services/shuttle/         |
| SEERStudio/VERDANDI/Chur/         | bff/chur/                 |
| SEERStudio/VERDANDI/verdandi/     | frontends/verdandi/       |
| SEERStudio/VERDANDI/keycloak/     | infra/keycloak/           |
| SEERStudio/VERDANDI/scripts/      | scripts/                  |
| SEERStudio/VERDANDI/docker-compose.yml | docker-compose.yml    |
| SEERStudio/VERDANDI/.github/      | .github/                  |
| SEERStudio/VERDANDI/current_pharse/ | docs/sprints/           |
| SEERStudio/VERDANDI/internal_docs/  | docs/internal/          |

### Build changes
- **SHUTTLE**: removed stale `pom.xml` (Quarkus 3.17.7), updated `settings.gradle` to standalone root
- **Hound**: added `java-library` plugin, changed `${rootDir}` → `${projectDir}` for ANTLR JAR path
- **docker-compose**: network renamed `verdandi_net` → `aida_net`, build contexts updated to new paths
- **CI**: added Hound test job, updated `working-directory` paths for all jobs

### Standalone IDEA support
Each JVM module (Hound, SHUTTLE) retains its own `gradlew`, `settings.gradle`, `build.gradle`,
and `gradle.properties`. They can be opened as standalone projects in IntelliJ IDEA.

### Removed
- `SHUTTLE/pom.xml` (stale Quarkus 3.17.7, unused — Gradle is the active build system)
- `.claude/` and `.vite/` from VERDANDI root (dev-specific, not needed in monorepo)
- Top-level `node_modules/` in old VERDANDI root (accidental `npm install`)

## Rollback
If critical issues arise, the old structure is available:
1. Pre-migration tag: `pre-migration` in old VERDANDI repo
2. Backup zip of `C:\AIDA` (created before migration)
3. Old repo: github.com/NooriUta/Verdandi (archived, read-only)
