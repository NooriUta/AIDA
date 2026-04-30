FROM node:24-alpine AS builder
RUN npm install -g npm@latest
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY tsconfig.json ./
COPY src ./src
# Cache-bust: include a timestamp in the src copy layer label so Docker
# always recompiles when source changes (Windows Docker Desktop doesn't
# always detect mtime changes across the Windows/Linux boundary).
ARG BUILDTIME=unknown
LABEL buildtime="${BUILDTIME}"
RUN npm run build

FROM node:24-alpine AS runner
RUN npm install -g npm@latest && apk upgrade --no-cache
WORKDIR /app
ENV NODE_ENV=production
COPY package*.json ./
RUN npm ci --omit=dev
COPY --from=builder /app/dist ./dist
EXPOSE 3000
CMD ["node", "dist/server.js"]
