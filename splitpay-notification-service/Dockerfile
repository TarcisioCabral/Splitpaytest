FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY splitpay-notification-service/pom.xml splitpay-notification-service/
RUN mvn dependency:go-offline -pl splitpay-notification-service -am
COPY splitpay-notification-service/src splitpay-notification-service/src
RUN mvn package -DskipTests -pl splitpay-notification-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/splitpay-notification-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
