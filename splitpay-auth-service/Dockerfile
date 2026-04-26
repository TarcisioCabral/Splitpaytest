# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy parent POM first for dependency caching
COPY pom.xml /app/pom.xml
COPY splitpay-api-gateway/pom.xml /app/splitpay-api-gateway/pom.xml
COPY splitpay-transaction-service/pom.xml /app/splitpay-transaction-service/pom.xml
COPY splitpay-conciliation-service/pom.xml /app/splitpay-conciliation-service/pom.xml
COPY splitpay-auth-service/pom.xml /app/splitpay-auth-service/pom.xml
COPY splitpay-frontend/pom.xml /app/splitpay-frontend/pom.xml

# Download dependencies
RUN apk add --no-cache maven && \
    mvn -f /app/pom.xml dependency:go-offline -pl splitpay-auth-service -am -q

# Copy source and build
COPY splitpay-auth-service/src /app/splitpay-auth-service/src
RUN mvn -f /app/pom.xml package -pl splitpay-auth-service -am -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

LABEL org.opencontainers.image.title="Splitpay Auth Service"
LABEL org.opencontainers.image.description="Authentication Service with JWT and H2"

# Non-root user
RUN addgroup -S splitpay && adduser -S splitpay -G splitpay
USER splitpay

COPY --from=builder /app/splitpay-auth-service/target/splitpay-auth-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
