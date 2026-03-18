FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY ../gradlew .
COPY ../gradle gradle
COPY ../build.gradle.kts .
COPY ../settings.gradle.kts .
COPY ../gradle.properties .
COPY movie-service/build.gradle.kts movie-service/
COPY movie-service/src movie-service/src

RUN ./gradlew :movie-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/movie-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
