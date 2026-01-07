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
RUN ./gradlew --no-daemon --stacktrace tasks || true

# Agora copia o resto
COPY . .

# Build com memória controlada
RUN ./gradlew :app:bootJar \
  --no-daemon \
  --stacktrace \
  -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=256m"

WORKDIR /app

FROM eclipse-temurin:21-jre-jammy AS runtime
COPY --from=build /app/app/build/libs/app-*.jar app.jar
COPY conteudo/ /conteudo/

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75", \
  "-XX:+UseG1GC", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]
