# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy parent POM first for dependency caching
COPY pom.xml /app/pom.xml
COPY splitpay-api-gateway/pom.xml /app/splitpay-api-gateway/pom.xml
COPY splitpay-transaction-service/pom.xml /app/splitpay-transaction-service/pom.xml
COPY splitpay-conciliation-service/pom.xml /app/splitpay-conciliation-service/pom.xml
COPY splitpay-frontend/pom.xml /app/splitpay-frontend/pom.xml

# Download dependencies (leverages Docker layer cache)
RUN apk add --no-cache maven && \
    mvn -f /app/pom.xml dependency:go-offline -pl splitpay-transaction-service -am -q

# Copy source and build only this module
COPY splitpay-transaction-service/src /app/splitpay-transaction-service/src
RUN mvn -f /app/pom.xml package -pl splitpay-transaction-service -am -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

LABEL org.opencontainers.image.title="Splitpay Transaction Service"
LABEL org.opencontainers.image.description="Transaction processing with PostgreSQL, RabbitMQ and Flyway"

# Non-root user for security
RUN addgroup -S splitpay && adduser -S splitpay -G splitpay
USER splitpay

COPY --from=builder /app/splitpay-transaction-service/target/transaction-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
