FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build
COPY pom.xml ./pom.xml
COPY order-service/pom.xml ./order-service/pom.xml
COPY payment-service/pom.xml ./payment-service/pom.xml
COPY shipping-service/pom.xml ./shipping-service/pom.xml
COPY analytics-service/pom.xml ./analytics-service/pom.xml
RUN mvn dependency:go-offline -pl shipping-service -am -q
COPY shipping-service/src ./shipping-service/src
RUN mvn package -pl shipping-service -am -DskipTests && rm -rf /root/.m2/repository

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/shipping-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
