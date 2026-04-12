###############################################################################
# HEIMDALL backend — multi-stage Docker build
#
# ВАЖНО: build context = монорепо root (aida-root/), НЕ services/heimdall-backend/.
# В docker-compose.yml:
#   heimdall-backend:
#     build:
#       context: .
#       dockerfile: services/heimdall-backend/Dockerfile
#
# Причина: HEIMDALL зависит от :shared:dali-models, поэтому в контексте
# должны быть оба модуля. Используем docker-settings.gradle чтобы Gradle
# не пытался собрать libraries:hound и services:shuttle.
###############################################################################

FROM eclipse-temurin:21-jdk-alpine AS builder
RUN apk add --no-cache dos2unix

WORKDIR /app

# ── Gradle-инфраструктура монорепо ────────────────────────────────────────────
COPY gradle           gradle
COPY gradlew          gradlew
COPY gradle.properties gradle.properties

# ── Docker-only overrides (подменяем root settings + build) ──────────────────
COPY services/heimdall-backend/docker-settings.gradle  settings.gradle
COPY services/heimdall-backend/docker-build.gradle     build.gradle

# ── Исходные модули, нужные этому сервису ────────────────────────────────────
COPY shared/dali-models                                shared/dali-models
COPY services/heimdall-backend/build.gradle            services/heimdall-backend/build.gradle
COPY services/heimdall-backend/gradle.properties       services/heimdall-backend/gradle.properties
COPY services/heimdall-backend/src                     services/heimdall-backend/src

RUN dos2unix gradlew && chmod +x gradlew
RUN ./gradlew :services:heimdall-backend:build -x test \
    -Dquarkus.package.jar.type=uber-jar

# ── Runtime image ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/services/heimdall-backend/build/*-runner.jar app.jar
EXPOSE 9093
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
