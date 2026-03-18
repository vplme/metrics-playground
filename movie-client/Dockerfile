FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY ../gradlew .
COPY ../gradle gradle
COPY ../build.gradle.kts .
COPY ../settings.gradle.kts .
COPY ../gradle.properties .
COPY movie-client/build.gradle.kts movie-client/
COPY movie-client/src movie-client/src
# movie-service build.gradle.kts needed for settings to resolve
COPY movie-service/build.gradle.kts movie-service/

RUN ./gradlew :movie-client:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/movie-client/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
