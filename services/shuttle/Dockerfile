FROM eclipse-temurin:21-jdk-alpine AS builder
RUN apk add --no-cache dos2unix
WORKDIR /app
COPY gradle         gradle
COPY gradlew        gradlew
COPY build.gradle   build.gradle
COPY settings.gradle settings.gradle
COPY gradle.properties gradle.properties
RUN dos2unix gradlew && chmod +x gradlew && ./gradlew --version
COPY src src
RUN ./gradlew build -x test -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/build/*-runner.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
