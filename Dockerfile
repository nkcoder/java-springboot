# Build stage
FROM openjdk:21-jdk-slim AS build

WORKDIR /app

# Copy Gradle wrapper and buf-gen files
COPY gradle/ gradle/
COPY gradlew build.gradle.kts ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew clean bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from buf-gen stage
COPY --from=build /app/build/libs/user-service.jar user-service.jar

# Expose port
EXPOSE 3001

# Run the application
CMD ["java", "-jar", "user-service.jar"]