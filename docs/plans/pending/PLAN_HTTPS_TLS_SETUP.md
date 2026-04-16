# HTTPS / TLS — Production Setup Plan

**Документ:** `PLAN_HTTPS_TLS_SETUP`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** PENDING — следующий спринт
**Приоритет:** 🔴 BLOCKER (login не работает без HTTPS из-за Secure cookie)

---

## 1. Проблема

Без HTTPS невозможна нормальная работа продакшена:

| Симптом | Причина |
|---|---|
| Login "сбрасывается" на Verdandi и Heimdall | `Secure` cookie браузер принимает только по HTTPS |
| `sameSite: strict` при редиректах KC | HTTPS обязателен для strict same-site |
| Данные в открытом виде по сети | HTTP-трафик не зашифрован |

Временный обход `COOKIE_SECURE=false` активен (PR #21). Нужно убрать его
как только TLS готов.

---

## 2. Целевая архитектура

```
Internet
   │  :443 (HTTPS)
   ▼
[host nginx — TLS termination]  ← cert from Let's Encrypt / YC Certificate Manager
   │
   ├─ /                  → verdandi:5173      (React SPA)
   ├─ /shell/            → shell:5175         (Module Federation host)
   ├─ /heimdall/         → heimdall-frontend:5174
   ├─ /auth/             → chur:3000
   ├─ /api/              → chur:3000
   ├─ /graphql           → chur:3000
   └─ /dali/             → chur:3000          (после auth-gate sprint)

Docker containers — внутренняя сеть aida_net (HTTP, без TLS)
Keycloak — KC_PROXY=edge уже выставлен (доверяет X-Forwarded-* от nginx)
```

> Порты 15173 / 25174 / 25175 / 13000 / 18080 закрываются firewall после
> включения TLS. Снаружи только 80 (redirect) и 443.

---

## 3. Предварительные требования

- [ ] Доменное имя направлено на IP VM (A-запись, TTL ≤ 300s)  
  DNS: `${DOMAIN}` → `111.88.242.194`
- [ ] Порты 80 и 443 открыты в YC Security Group / firewall
- [ ] На VM установлен certbot: `sudo apt install certbot python3-certbot-nginx`

---

## 4. Шаги реализации

### 4.1 Host nginx + Let's Encrypt

```bash
# На VM (ubuntu)
sudo apt update && sudo apt install -y nginx certbot python3-certbot-nginx

# Получить сертификат
sudo certbot --nginx -d ${DOMAIN} --non-interactive --agree-tos -m admin@${DOMAIN}

# Certbot сам добавит TLS в nginx конфиг. Проверить auto-renew:
sudo systemctl enable --now certbot.timer
```

**`/etc/nginx/sites-available/aida`** (создать):

```nginx
# HTTP → HTTPS redirect
server {
    listen 80;
    server_name ${DOMAIN};
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name ${DOMAIN};

    ssl_certificate     /etc/letsencrypt/live/${DOMAIN}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${DOMAIN}/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    # Forwarded headers для Keycloak (KC_PROXY=edge уже выставлен)
    proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto https;
    proxy_set_header X-Forwarded-Host  $host;
    proxy_set_header Host              $host;

    # Verdandi (React SPA)
    location / {
        proxy_pass http://127.0.0.1:15173;
    }

    # Shell (Module Federation host) — если нужен отдельный путь
    location /shell/ {
        proxy_pass http://127.0.0.1:25175/;
    }

    # Heimdall admin UI
    location /heimdall/ {
        proxy_pass http://127.0.0.1:25174/;
    }

    # WebSocket (для Heimdall event stream)
    location /heimdall/ws/ {
        proxy_pass          http://127.0.0.1:25174/heimdall/ws/;
        proxy_http_version  1.1;
        proxy_set_header    Upgrade    $http_upgrade;
        proxy_set_header    Connection "upgrade";
        proxy_read_timeout  3600s;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/aida /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

### 4.2 Keycloak — обновить KC_HOSTNAME

В `docker-compose.yc.yml` → keycloak environment:
```yaml
KC_HOSTNAME: "https://${DOMAIN}"   # вместо KC_HOSTNAME_STRICT: "false"
KC_HOSTNAME_STRICT: "true"
```

Также обновить `Valid redirect URIs` в seer-realm.json:
```json
"redirectUris": ["https://${DOMAIN}/*", "http://localhost:*"]
```

### 4.3 Chur — включить Secure cookies

В `docker-compose.yc.yml` → chur environment:
```yaml
COOKIE_SECURE: "true"      # ← убрать "false"
CORS_ORIGIN: "https://${DOMAIN}"
```

В `docker-compose.prod.yml` → chur environment:
```yaml
CORS_ORIGIN: "https://${DOMAIN},http://localhost:15173"
```

### 4.4 Verdandi / Heimdall — обновить базовые URL

В `docker-compose.prod.yml` → verdandi/heimdall-frontend: без изменений
(nginx внутри контейнеров уже проксирует на chur по внутренней сети).

### 4.5 Закрыть прямые порты

В YC Security Group (или `ufw`) закрыть внешний доступ к:
```
15173, 25174, 25175, 13000, 18080, 18180, 2480, 2481, 19090, 19093
```
Оставить открытыми только: **80, 443, 22** (SSH).

### 4.6 CD pipeline — добавить деплой nginx конфига

В `.github/workflows/cd.yml` → deploy step добавить:
```bash
# Обновить nginx конфиг на VM (если изменился)
scp infra/nginx/aida.conf ubuntu@${DEPLOY_HOST}:/tmp/aida.conf
ssh ubuntu@${DEPLOY_HOST} "sudo cp /tmp/aida.conf /etc/nginx/sites-available/aida && sudo nginx -t && sudo systemctl reload nginx"
```

---

## 5. Что изменить в коде

| Файл | Изменение |
|---|---|
| `docker-compose.yc.yml` | `KC_HOSTNAME`, `COOKIE_SECURE: "true"`, `CORS_ORIGIN` |
| `docker-compose.prod.yml` | `CORS_ORIGIN` с prod доменом |
| `infra/keycloak/seer-realm.json` | `redirectUris` добавить `https://${DOMAIN}/*` |
| `infra/nginx/aida.conf` | Новый файл — host nginx конфиг (TLS termination) |
| `.github/workflows/cd.yml` | Деплой nginx конфига на VM |

---

## 6. Acceptance Criteria

```
✅ https://${DOMAIN}/ открывается, сертификат валидный (Let's Encrypt)
✅ http://${DOMAIN}/ редиректит на https://
✅ Login на Verdandi (/) и Heimdall (/heimdall/) работает
✅ Session cookie Secure=true, SameSite=Strict в DevTools
✅ Прямые порты (15173, 25174 и др.) недоступны снаружи
✅ certbot renew --dry-run проходит без ошибок
✅ KC redirect_uri не содержит localhost в prod логах
```

---

## 7. Не входит в этот спринт

- mTLS между контейнерами (over-engineering для M2)
- Let's Encrypt wildcard cert (DNS-01 challenge — усложнение)
- Cloudflare / YC CDN (V2)
