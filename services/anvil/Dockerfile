###############################################################################
# ANVIL — multi-stage Docker build
#
# ВАЖНО: build context = монорепо root (aida-root/), НЕ services/anvil/.
# В docker-compose.prod.yml:
#   anvil:
#     build:
#       context: .
#       dockerfile: services/anvil/Dockerfile
#
# Anvil depends on :shared:dali-models (shared event types).
# docker-settings.gradle scopes Gradle to these two modules only.
###############################################################################

FROM gradle:9-jdk21-alpine AS builder
WORKDIR /app

COPY gradle.properties                          gradle.properties
COPY services/anvil/docker-settings.gradle     settings.gradle
COPY shared/dali-models                        shared/dali-models
COPY services/anvil/build.gradle               services/anvil/build.gradle
COPY services/anvil/gradle.properties          services/anvil/gradle.properties
# Dependency cache layer — rebuilt only when build.gradle / dali-models change.
RUN gradle :services:anvil:dependencies --no-daemon -q
COPY services/anvil/src                        services/anvil/src

RUN gradle :services:anvil:build -x test --no-daemon \
    -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21.0.9_10-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/services/anvil/build/*-runner.jar app.jar
EXPOSE 9095
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
