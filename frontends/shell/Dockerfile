# Build context must be the monorepo root (to include packages/aida-shared).
# docker-compose sets:  context: .  dockerfile: frontends/shell/Dockerfile

FROM node:24-alpine AS builder
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
RUN rm -rf .vite && npx vite build

FROM nginx:alpine AS runner
COPY --from=builder /app/frontends/shell/dist /usr/share/nginx/html
COPY frontends/shell/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 5175
