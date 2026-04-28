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
# Причина: HEIMDALL зависит от :shared:dali-models (inter-module dependency).
# Используем docker-settings.gradle чтобы Gradle не пытался собрать
# libraries:hound и services:shuttle.
###############################################################################

FROM gradle:9-jdk21-alpine AS builder
WORKDIR /app

COPY gradle.properties                                 gradle.properties
COPY services/heimdall-backend/docker-settings.gradle settings.gradle
COPY services/heimdall-backend/docker-build.gradle    build.gradle
COPY shared/dali-models                               shared/dali-models
COPY services/heimdall-backend/build.gradle           services/heimdall-backend/build.gradle
COPY services/heimdall-backend/gradle.properties      services/heimdall-backend/gradle.properties
# Dependency cache layer — rebuilt only when build.gradle / dali-models change.
# src is copied separately below so code changes don't bust this layer.
RUN gradle :services:heimdall-backend:dependencies --no-daemon -q
COPY services/heimdall-backend/src                    services/heimdall-backend/src

RUN gradle :services:heimdall-backend:build -x test --no-daemon \
    -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21.0.9_10-jre-alpine AS runner
RUN apk add --no-cache docker-cli
WORKDIR /app
COPY --from=builder /app/services/heimdall-backend/build/*-runner.jar app.jar
EXPOSE 9093
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
