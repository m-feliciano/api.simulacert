FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /workspace

# Copy only the necessary files for dependency resolution to leverage Docker caching
COPY gradlew .
COPY gradle ./gradle
COPY settings.gradle .
COPY build.gradle .
COPY app/build.gradle app/
COPY auth/build.gradle auth/
COPY attempt/build.gradle attempt/
COPY common/build.gradle common/
COPY exam/build.gradle exam/
COPY llm/build.gradle llm/
COPY review/build.gradle review/
COPY stats/build.gradle stats/
COPY translation/build.gradle translation/

RUN chmod +x gradlew

RUN ./gradlew :app:dependencies --no-daemon > /dev/null || true

# Now copy the rest of the source code
COPY . .

RUN ./gradlew :app:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends ca-certificates \
  && rm -rf /var/lib/apt/lists/* \
  && groupadd -r appuser && useradd -r -g appuser appuser \
  && mkdir -p /app/logs \
  && chown -R appuser:appuser /app

COPY --from=builder /workspace/app/build/libs/app.jar /app/app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=UTC", \
  "-jar", \
  "app.jar"]
