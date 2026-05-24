FROM gradle:8.14.3-jdk21 AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
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

RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew dependencies --no-daemon

COPY . .

RUN --mount=type=cache,target=/home/gradle/.gradle \
    ./gradlew :app:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=builder /workspace/app/build/libs/app.jar app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=UTC", \
  "-jar", \
  "app.jar"]