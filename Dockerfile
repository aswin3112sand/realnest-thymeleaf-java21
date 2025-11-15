# Multi-stage build for the RealNest Spring Boot app
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy source, configs, and Maven wrapper if available
COPY . .

# Build with mvnw when present, otherwise fallback to mvn
RUN set -eux; \
    if [ -f mvnw ]; then \
      chmod +x mvnw; \
      ./mvnw -B -DskipTests clean package; \
    else \
      mvn -B -DskipTests clean package; \
    fi; \
    JAR_PATH=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*original*" | head -n 1); \
    test -n "$JAR_PATH"; \
    cp "$JAR_PATH" /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
ENV PORT=8080
EXPOSE 8080

COPY --from=builder /workspace/app.jar ./app.jar

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
