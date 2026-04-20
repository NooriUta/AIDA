# HEIMDALL — Sources Page (UI Guide)

**Version:** 1.0  
**Date:** 2026-04-20  
**Status:** ✅ Released (v1.2.0)  
**Component:** `frontends/heimdall-frontend/src/pages/DaliSourcesPage.tsx`

---

## Overview

The Sources page (`/sources`) provides two independent data-ingestion modes:

| Mode | Use Case | Trigger |
|------|----------|---------|
| **UC-1a: SKADI Harvest (JDBC)** | Connect to a remote DB, schedule recurring harvest | `HarvestJob` via cron / API |
| **UC-1b: File Upload** | Upload local SQL/ZIP files for one-shot parse | `POST /api/sessions/upload` |

---

## UC-1a: SKADI Harvest (JDBC Sources)

### Sources Table

Lists all configured JDBC sources from `GET /api/sources`.

Columns: **Name** · **Dialect** · **JDBC URL** · **Last Harvest** · **Atom Count** · **Actions (Edit / Delete / Test)**

### Add / Edit Source Modal

Fields:

| Field | Type | Notes |
|-------|------|-------|
| Name | text | unique identifier |
| Dialect | select | `oracle` / `postgresql` / `clickhouse` |
| JDBC URL | text | e.g. `jdbc:oracle:thin:@host:1521:SID` |
| Username | text | DB user (e.g. `dali_harvest`) |
| Password | password | stored encrypted in FRIGG |
| Schema Filter — Include | tag input | empty = all; e.g. `HR`, `FINANCE` |
| Schema Filter — Exclude | tag input | pre-filled with dialect defaults |

**Schema Filter behavior:**
- When **Dialect** changes, **Exclude** tags auto-reset to `DEFAULT_EXCLUSIONS[dialect]`
- **Include** empty = harvest all schemas except excluded
- **Exclude** overrides include: a schema in both lists is excluded

**Default Exclude per dialect:**

| Dialect | Defaults |
|---------|---------|
| Oracle | `SYS, SYSTEM, DBSNMP, OUTLN, MDSYS, ORDSYS, XDB, WMSYS, CTXSYS, ANONYMOUS, APPQOSSYS, MGMT_VIEW, EXFSYS, DMSYS, OJVMSYS, GSMADMIN_INTERNAL` |
| PostgreSQL | `information_schema, pg_catalog, pg_toast, pg_temp` |
| ClickHouse | `system, information_schema, INFORMATION_SCHEMA` |

### ⚡ Test Connection

Button in modal → `POST /api/sources/test`

Success: `{ ok: true, latencyMs: 42 }` → shows green badge  
Failure: `{ ok: false, error: "..." }` → shows red error text

### Actions

| Button | Action |
|--------|--------|
| `+ Add Source` | Opens empty modal |
| `✎ Edit` | Opens modal pre-filled with source data |
| `⚡ Test` | Inline connection test (no modal) |
| `🗑 Delete` | Confirm dialog → `DELETE /api/sources/{id}` |
| `⚡ Harvest All` | `POST /sessions` with `sourceFilter: null` |

---

## UC-1b: File Upload

### Upload Card

Located below the Sources table. Fields:

| Field | Type | Notes |
|-------|------|-------|
| File drop zone | drag & drop / click | `.sql .pck .prc .pkb .pks .fnc .trg .vw .zip .rar` |
| Dialect | select | `plsql` (default) / `postgresql` / `clickhouse` |
| Preview mode | checkbox | if checked, parse without writing to YGG |
| Clear YGG before write | checkbox | default: checked |

**Upload button** → `POST /api/sessions/upload` (multipart)

On success: navigates to **Sessions** tab showing the new upload session.

---

## Navigation

The HEIMDALL left sidebar links:

```
Dashboard      /
Sources        /sources      ← this page
Sessions       /sessions
JobRunr        /jobrunr
Docs           /docs
```

---

## Backend Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/sources` | List all JDBC sources |
| `POST` | `/api/sources` | Create source |
| `PUT` | `/api/sources/{id}` | Update source (incl. schemaFilter) |
| `DELETE` | `/api/sources/{id}` | Delete source |
| `POST` | `/api/sources/test` | Test JDBC connection |
| `POST` | `/api/sessions/upload` | Upload SQL/ZIP for one-shot parse |
| `POST` | `/sessions` | Trigger JDBC harvest session |
