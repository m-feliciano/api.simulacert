# =========================
# Build stage
# =========================
FROM --platform=linux/arm64 gradle:8.11-jdk21 AS build
WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN ./gradlew :app:bootJar \
  --no-daemon \
  -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=312m"

# =========================
# Runtime stage
# =========================
FROM --platform=linux/arm64 eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=build /app/app/build/libs/app.jar app.jar
COPY conteudo/ /conteudo/

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

USER appuser

ENTRYPOINT ["java",
  "-XX:+UseContainerSupport",
  "-XX:+UseG1GC",
  "-XX:MaxGCPauseMillis=200",
  "-XX:InitiatingHeapOccupancyPercent=30",
  "-XX:+ParallelRefProcEnabled",
  "-XX:+DisableExplicitGC",
  "-XX:+HeapDumpOnOutOfMemoryError",
  "-XX:+ExitOnOutOfMemoryError",
  "-Dfile.encoding=UTF-8",
  "-Duser.timezone=UTC",
  "-jar",
  "app.jar"]