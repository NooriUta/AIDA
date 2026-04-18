# Branching Strategy — AIDA Monorepo

## Model: Gitflow

```
master ──────────────────────────────●─────────── (v1.0.0 tag → CD deploy)
          ↑ merge release/1.0.0       ↑ merge hotfix/xxx
develop ──●──────────────────────────●────────────
          ↑ merge feature/xxx         ↑ merge fix/xxx
feature/xxx ───────                  fix/xxx ──────
```

---

## Branch Types

| Branch | Created from | Merges into | Purpose |
|---|---|---|---|
| `master` | — | — | Stable production. Tagged on every release. |
| `develop` | `master` | — | Integration branch. All features land here first. |
| `feature/<name>` | `develop` | `develop` | New functionality. |
| `fix/<name>` | `develop` | `develop` | Bug fix (non-critical). |
| `release/<x.y.z>` | `develop` | `master` + `develop` | Release preparation: version bumps, last fixes only. |
| `hotfix/<name>` | `master` | `master` + `develop` | Critical prod fix — bypasses develop. |
| `setup/<name>` | `master`/`develop` | `master`/`develop` | Tooling / infrastructure changes. |

---

## Day-to-Day Workflows

### New feature

```bash
git checkout develop && git pull origin develop
git checkout -b feature/my-feature
# work...
git push origin feature/my-feature
# open PR → develop
```

### Bug fix

```bash
git checkout develop && git pull origin develop
git checkout -b fix/some-bug
# work...
git push origin fix/some-bug
# open PR → develop
```

### Preparing a release

```bash
git checkout develop && git pull origin develop
git checkout -b release/1.1.0
# bump versions, final tweaks only — no new features
git push origin release/1.1.0
# open PR → master
# after merge, also merge back into develop:
git checkout develop
git merge master
git push origin develop
```

### Hotfix (critical prod bug)

```bash
git checkout master && git pull origin master
git checkout -b hotfix/critical-fix
# fix...
git push origin hotfix/critical-fix
# open PR → master (and backport PR → develop)
```

---

## Creating a Release

1. Finish all features for the milestone in `develop`.
2. Create `release/x.y.z` branch from `develop`.
3. Open PR `release/x.y.z → master`.
4. After CI passes and PR is merged, tag master:

```bash
git checkout master && git pull origin master
git tag -a vX.Y.Z -m "chore(release): vX.Y.Z"
git push origin vX.Y.Z
```

5. Two GitHub Actions workflows fire automatically:
   - **Release** — generates CHANGELOG, creates GitHub Release `vX.Y.Z`.
   - **CD** — builds Docker images tagged `vX.Y.Z`, deploys to production.

6. Merge master back into develop (to pick up the CHANGELOG commit):

```bash
git checkout develop && git pull origin develop
git merge master
git push origin develop
```

---

## Commit Message Format (Conventional Commits)

```
type(scope): short description (max 100 chars)

[optional body]

[optional footer: Closes #123]
```

### Types

| Type | When |
|---|---|
| `feat` | New user-facing feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `refactor` | Code change without behavior change |
| `perf` | Performance improvement |
| `test` | Adding/updating tests |
| `chore` | Build, tooling, dependencies |
| `ci` | CI/CD changes |
| `build` | Build system changes |
| `revert` | Reverts a previous commit |

### Examples

```
feat(dali): add file upload endpoint for SQL artifacts
fix(chur): correct CORS headers for seer subdomain
docs(guides): add branching strategy doc
chore(deps): bump quarkus to 3.9.0
ci(cd): switch CD trigger to semver tags
```

The `commit-msg` git hook (husky + commitlint) enforces this format locally.
Bypass only in emergencies: `git commit --no-verify`.

---

## Branch Protection Summary

| Branch | Direct push | Force push | Requires |
|---|---|---|---|
| `master` | Blocked | Blocked | PR + CI pass |
| `develop` | Allowed (solo dev) | Blocked | CI pass on PR |

---

## Versioning

Format: `vMAJOR.MINOR.PATCH`

| Increment | When |
|---|---|
| PATCH (0.0.**x**) | Bug fixes, no new features |
| MINOR (0.**x**.0) | New backwards-compatible features |
| MAJOR (**x**.0.0) | Breaking changes |

Docker images are tagged: `vX.Y.Z` (primary), `<sha>` (traceability), `latest`.

Rollback: `./scripts/rollback.sh vX.Y.Z`
