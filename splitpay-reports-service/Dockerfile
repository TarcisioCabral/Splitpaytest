# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy parent POM first for dependency caching
COPY pom.xml /app/pom.xml
COPY splitpay-api-gateway/pom.xml /app/splitpay-api-gateway/pom.xml
COPY splitpay-transaction-service/pom.xml /app/splitpay-transaction-service/pom.xml
COPY splitpay-conciliation-service/pom.xml /app/splitpay-conciliation-service/pom.xml
COPY splitpay-frontend/pom.xml /app/splitpay-frontend/pom.xml
COPY splitpay-auth-service/pom.xml /app/splitpay-auth-service/pom.xml
COPY splitpay-reports-service/pom.xml /app/splitpay-reports-service/pom.xml

# Download dependencies (leverages Docker layer cache)
RUN apk add --no-cache maven && \
    mvn -f /app/pom.xml dependency:go-offline -pl reports-service -am -q

# Copy source and build only this module
COPY splitpay-reports-service/src /app/splitpay-reports-service/src
RUN mvn -f /app/pom.xml package -pl reports-service -am -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

LABEL org.opencontainers.image.title="Splitpay Reports Service"
LABEL org.opencontainers.image.description="Analytics and trend reporting for taxes"

# Non-root user for security
RUN addgroup -S splitpay && adduser -S splitpay -G splitpay
USER splitpay

COPY --from=builder /app/splitpay-reports-service/target/reports-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
