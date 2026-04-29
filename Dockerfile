FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends curl ca-certificates \
  && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY app.jar app.jar
COPY conteudo/ /conteudo/

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=UTC", \
  "-jar", \
  "app.jar"]
