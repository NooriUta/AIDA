# Build context must be the monorepo root (to include packages/aida-shared).
# docker-compose sets:  context: .  dockerfile: frontends/shell/Dockerfile

FROM node:24-alpine AS builder
WORKDIR /app

# Copy shared package first so npm can resolve file: dependency
COPY packages/aida-shared ./packages/aida-shared

WORKDIR /app/frontends/shell
COPY frontends/shell/package*.json ./
RUN npm ci --legacy-peer-deps

COPY frontends/shell/ .
RUN npx vite build

FROM nginx:alpine AS runner
COPY --from=builder /app/frontends/shell/dist /usr/share/nginx/html
COPY frontends/shell/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 5175
