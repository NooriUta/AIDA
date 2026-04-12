FROM gradle:9-jdk21-alpine AS builder
WORKDIR /app
COPY build.gradle settings.gradle gradle.properties ./
COPY src src
RUN gradle build -x test --no-daemon -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/build/*-runner.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
