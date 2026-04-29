###############################################################################
# SHUTTLE — multi-stage Docker build
#
# ВАЖНО: build context = монорепо root (aida-root/), НЕ services/shuttle/.
# В docker-compose.backend.yml:
#   shuttle:
#     build:
#       context: .
#       dockerfile: services/shuttle/Dockerfile
#
# Причина: SHUTTLE зависит от :libraries:tenant-routing
# (inter-module dependency). Используем docker-settings.gradle
# чтобы Gradle не пытался собрать лишние модули.
###############################################################################

FROM gradle:9-jdk21-alpine AS builder
WORKDIR /app

COPY gradle.properties                            gradle.properties
COPY services/shuttle/docker-settings.gradle      settings.gradle
COPY services/shuttle/build.gradle                services/shuttle/build.gradle
COPY services/shuttle/gradle.properties           services/shuttle/gradle.properties
COPY services/shuttle/src                         services/shuttle/src
COPY libraries/tenant-routing/build.gradle        libraries/tenant-routing/build.gradle
COPY libraries/tenant-routing/src                 libraries/tenant-routing/src

RUN gradle :services:shuttle:build -x test --no-daemon \
    -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21.0.9_10-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/services/shuttle/build/*-runner.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
