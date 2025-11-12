# Stage 1: Building the project
FROM maven:latest as builder
WORKDIR /app

# Copy Maven build files for dependency caching
COPY pom.xml ./
COPY src ./src

RUN mvn clean install -DskipTests

# Stage 2: Building final image
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar .

EXPOSE 8080

CMD ["java", "-jar", "inventorymanagementsystem-0.0.1-SNAPSHOT.jar"]