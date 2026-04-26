# Seiðr KC theme

Phase 1 scaffold — minimum-viable theme inheriting from KC `keycloak` (legacy server-rendered theme, NOT `keycloak.v2` which is JS-driven SPA).

## Mount in docker-compose.stable.yml

```yaml
keycloak:
  volumes:
    - ./infra/keycloak/themes:/opt/keycloak/themes:ro
```

## Activate in realm

`seer-realm.json` realm-level: `loginTheme: "seer"` (also `accountTheme`, `emailTheme`).

## Files

```
seer/
├── login/
│   ├── theme.properties        — parent=keycloak, locales en+ru
│   ├── messages/messages_*.properties
│   └── resources/css/seer.css  — amber-on-dark brandbook palette
├── email/                      — TODO: invite/password-reset templates
└── account/                    — TODO: account console
```

## To do (designer/frontend)

- [ ] Override `template.ftl` with seer header/footer (logo + nav)
- [ ] Custom `login.ftl` matching seer-studio.pro layout
- [ ] Email templates branded (invite-organization, executeActions)
- [ ] WebFonts: Unbounded 800 + Manrope 300-700 (CDN or self-hosted)
- [ ] Light/dark toggle (auto from prefers-color-scheme)
- [ ] Visual smoke against design ref

## Test theme locally

1. Start KC: `docker compose -f docker-compose.stable.yml up -d keycloak`
2. Browse: http://localhost:18180/kc/realms/seer/account
3. Hot-reload: KC start-dev restarts theme on file change (no container restart needed)
