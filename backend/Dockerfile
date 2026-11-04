FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY backend/ ./backend/

WORKDIR /workspace/backend

RUN chmod +x ./gradlew && \
    ./gradlew --no-daemon clean test bootJar && \
    JAR_FILE="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" && \
    test -n "$JAR_FILE" && \
    cp "$JAR_FILE" /workspace/app.jar

FROM eclipse-termurin:21-jre

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/app.jar /app/app.jar

ENV SERVER_PORT=8080

EXPOSE 8080

HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=40s \
            --retries=3 \
            CMD curl -fsS http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
