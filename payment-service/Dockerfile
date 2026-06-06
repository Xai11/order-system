FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml ./pom.xml
COPY order-service/pom.xml ./order-service/pom.xml
COPY payment-service/pom.xml ./payment-service/pom.xml
COPY shipping-service/pom.xml ./shipping-service/pom.xml
COPY analytics-service/pom.xml ./analytics-service/pom.xml
RUN mvn dependency:go-offline -pl payment-service -am -q
COPY payment-service/src ./payment-service/src
RUN mvn package -pl payment-service -am -DskipTests && rm -rf /root/.m2/repository

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/payment-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
