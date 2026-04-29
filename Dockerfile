# =========================
# Build stage
# =========================
FROM --platform=$BUILDPLATFORM gradle:8.11-jdk21 AS build
WORKDIR /app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle build.gradle ./
COPY app/build.gradle app/

RUN ./gradlew dependencies --no-daemon

COPY . .

RUN ./gradlew :app:bootJar \
  --no-daemon \
  -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=312m"

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -ms /bin/bash appuser

COPY --from=build /app/app/build/libs/app.jar app.jar
COPY conteudo/ /conteudo/

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

USER appuser

ENTRYPOINT ["java", \
              "-XX:+UseContainerSupport", \
              "-XX:+UseG1GC", \
              "-XX:MaxGCPauseMillis=200", \
              "-XX:InitiatingHeapOccupancyPercent=30", \
              "-XX:+ParallelRefProcEnabled", \
              "-XX:+DisableExplicitGC", \
              "-XX:+HeapDumpOnOutOfMemoryError", \
              "-XX:+ExitOnOutOfMemoryError", \
              "-Dfile.encoding=UTF-8", \
              "-Duser.timezone=UTC", \
              "-jar", "app.jar"]