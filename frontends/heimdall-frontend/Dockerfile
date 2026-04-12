# Build context must be the monorepo root — heimdall-frontend depends on packages/aida-shared.
# docker-compose sets:  context: .  dockerfile: frontends/heimdall-frontend/Dockerfile

FROM node:24-alpine AS builder
WORKDIR /app

COPY packages/aida-shared ./packages/aida-shared

WORKDIR /app/frontends/heimdall-frontend
COPY frontends/heimdall-frontend/package*.json ./
# Replace file: symlink with actual copy so CJS require() works in Alpine Docker
RUN npm ci --legacy-peer-deps && \
    rm -rf node_modules/aida-shared && \
    cp -rL /app/packages/aida-shared node_modules/aida-shared

COPY frontends/heimdall-frontend/ .
ARG VITE_HEIMDALL_API=""
ENV VITE_HEIMDALL_API=$VITE_HEIMDALL_API
RUN rm -rf .vite && npx vite build

FROM nginx:alpine AS runner
COPY --from=builder /app/frontends/heimdall-frontend/dist /usr/share/nginx/html
COPY frontends/heimdall-frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 5174
