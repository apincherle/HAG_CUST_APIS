# Build stage (matches pom.xml Java 17)
FROM eclipse-temurin:17-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage (aligned with docker-compose / Azure Web App for Containers)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/hags-customer-api-1.0-SNAPSHOT.jar app.jar

EXPOSE 8001

ENTRYPOINT ["java", "-jar", "app.jar"]
