# Build stage
FROM gradle:8.11-jdk21 AS build
WORKDIR /app

COPY . .

RUN gradle :app:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/app/build/libs/app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=85", \
  "-XX:InitialRAMPercentage=50", \
  "-XX:+UseG1GC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]