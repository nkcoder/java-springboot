# =============================================================================
# Multi-stage Dockerfile for Spring Boot Application
# =============================================================================
# Build: docker build -t user-service .
# Run:   docker run -p 3001:3001 -p 9090:9090 user-service
# =============================================================================

# -----------------------------------------------------------------------------
# Build Stage (use Eclipse Temurin with glibc for protoc compatibility)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradle/ gradle/
COPY gradlew build.gradle.kts ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (cached layer if build files unchanged)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code (including proto files)
COPY src ./src

# Build the application (generateProto runs automatically, skip tests)
RUN ./gradlew clean bootJar -x test --no-daemon

# -----------------------------------------------------------------------------
# Runtime Stage (use Alpine for smaller image size)
# -----------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-alpine

# Add non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy JAR from build stage (matches bootJar archiveFileName in build.gradle.kts)
COPY --from=build /app/build/libs/user-application.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose REST and gRPC ports
EXPOSE 3001 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3001/actuator/health || exit 1

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
