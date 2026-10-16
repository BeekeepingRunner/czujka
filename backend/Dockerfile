# Build stage: compile and package the Spring Boot jar
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon > /dev/null
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage: only the JRE and the jar, running as a non-root user
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 1001 czujka
USER czujka
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
