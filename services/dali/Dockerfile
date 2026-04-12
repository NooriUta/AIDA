###############################################################################
# Dali — multi-stage Docker build
#
# ВАЖНО: build context = монорепо root (aida-root/), НЕ services/dali/.
# В docker-compose.yml:
#   dali:
#     build:
#       context: .
#       dockerfile: services/dali/Dockerfile
#
# Причина: Dali зависит от :libraries:hound и :shared:dali-models
# (inter-module dependencies). Используем docker-settings.gradle
# чтобы Gradle не пытался собрать лишние модули.
###############################################################################

FROM gradle:9-jdk21-alpine AS builder
WORKDIR /app

COPY gradle.properties                          gradle.properties
COPY services/dali/docker-settings.gradle       settings.gradle
COPY services/dali/build.gradle                 services/dali/build.gradle
COPY services/dali/gradle.properties            services/dali/gradle.properties
COPY services/dali/src                          services/dali/src
COPY shared/dali-models                         shared/dali-models
COPY libraries/hound/build.gradle               libraries/hound/build.gradle
COPY libraries/hound/src                        libraries/hound/src

RUN gradle :services:dali:build -x test --no-daemon \
    -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/services/dali/build/*-runner.jar app.jar
EXPOSE 9090
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
