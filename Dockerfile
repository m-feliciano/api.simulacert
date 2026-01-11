# =========================
# Build stage
# =========================
FROM --platform=$BUILDPLATFORM gradle:8.11-jdk21 AS build
WORKDIR /app

# Copiar apenas o necessário primeiro
COPY gradle gradle
COPY gradlew .
COPY settings.gradle build.gradle ./
COPY app/build.gradle app/

# Pré-download de dependências
RUN ./gradlew --no-daemon tasks || true

# Agora copia o resto do código
COPY . .

# Build com memória controlada (não estoura o build container)
RUN ./gradlew :app:bootJar \
  --no-daemon \
  -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=312m"

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app


COPY --from=build /app/app/build/libs/app-*.jar app.jar
COPY conteudo/ /conteudo/

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=60", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:InitiatingHeapOccupancyPercent=30", \
  "-XX:+ParallelRefProcEnabled", \
  "-XX:+DisableExplicitGC", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]
