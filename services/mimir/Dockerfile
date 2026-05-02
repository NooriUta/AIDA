###############################################################################
# MIMIR — multi-stage Docker build
#
# ВАЖНО: build context = монорепо root (aida-root/), НЕ services/mimir/.
# В docker-compose.backend.yml:
#   mimir:
#     build:
#       context: .
#       dockerfile: services/mimir/Dockerfile
#
# Используем docker-settings.gradle чтобы Gradle не пытался собрать
# несвязанные модули монорепо.
###############################################################################

FROM gradle:9-jdk21-alpine AS builder
WORKDIR /app

COPY gradle.properties                       gradle.properties
COPY services/mimir/docker-settings.gradle  settings.gradle
COPY services/mimir/build.gradle            services/mimir/build.gradle
COPY services/mimir/gradle.properties       services/mimir/gradle.properties

# shared:dali-models — HeimdallEvent / EventType records used by MIMIR.
# Added in CORE sprint (PR #103) when MimirEventEmitter became real.
COPY shared/dali-models/build.gradle        shared/dali-models/build.gradle
COPY shared/dali-models/src                 shared/dali-models/src

RUN gradle :services:mimir:dependencies --no-daemon -q
COPY services/mimir/src                     services/mimir/src

RUN gradle :services:mimir:build -x test --no-daemon \
    -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21.0.9_10-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/services/mimir/build/*-runner.jar app.jar
EXPOSE 9094
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
