# Multi-stage Docker build for Waste Management Attendance System

# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install required packages
RUN apt-get update && apt-get install -y \
    libopencv-dev \
    libsm6 \
    libxext6 \
    libxrender-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Create necessary directories
RUN mkdir -p logs data/faces

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV APP_PORT=8080

# Expose port
EXPOSE 8080

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
