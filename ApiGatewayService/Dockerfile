# Dockerfile for api-gateway
FROM maven:3.8.6-amazoncorretto-17 AS build
COPY pom.xml /build/
WORKDIR /build/
RUN mvn dependency:go-offline
COPY src /build/src/
RUN mvn package -DskipTests

# Run stage
FROM openjdk:17-alpine
COPY --from=build /build/target/*.jar /opt/docker-apigateway/apigateway.jar
ENTRYPOINT ["java", "-jar", "/opt/docker-apigateway/apigateway.jar"]