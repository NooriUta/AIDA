# Build context must be the monorepo root (to include packages/aida-shared).
# docker-compose sets:  context: .  dockerfile: frontends/shell/Dockerfile

FROM node:24-alpine AS builder
RUN npm install -g npm@latest
WORKDIR /app

# Copy shared package first so npm can resolve file: dependency
COPY packages/aida-shared ./packages/aida-shared

WORKDIR /app/frontends/shell
COPY frontends/shell/package*.json ./
# npm ci creates a symlink for file: deps — Alpine Docker doesn't follow it reliably.
# Replace the symlink with an actual copy so CJS require() and Vite alias both work.
RUN npm ci --legacy-peer-deps && \
    rm -rf node_modules/aida-shared && \
    cp -rL /app/packages/aida-shared node_modules/aida-shared

COPY frontends/shell/ .

# Build-time remote entry URLs — CD passes DOMAIN=seidrstudio.pro as build-arg.
# VITE_VERDANDI_URL / VITE_HEIMDALL_URL can be overridden explicitly if needed.
ARG DOMAIN=seidrstudio.pro
ARG VITE_VERDANDI_URL
ARG VITE_HEIMDALL_URL
RUN rm -rf .vite && \
    VITE_VERDANDI_URL="${VITE_VERDANDI_URL:-https://seer.${DOMAIN}/remoteEntry.js}" \
    VITE_HEIMDALL_URL="${VITE_HEIMDALL_URL:-https://heimdall.${DOMAIN}/remoteEntry.js}" \
    npx vite build

FROM nginx:alpine AS runner
RUN apk upgrade --no-cache
COPY --from=builder /app/frontends/shell/dist /usr/share/nginx/html
COPY frontends/shell/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 5175
