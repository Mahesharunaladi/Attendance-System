#!/bin/bash

# Attendance System - Backend Startup Script
# This script will start the backend server with all necessary checks

set -e

PROJECT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAR_FILE="$PROJECT_DIR/target/attendance-system-face-recognition-1.0.0.jar"

echo "=========================================="
echo "Attendance System - Backend Startup"
echo "=========================================="
echo ""

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "Building the project..."
    cd "$PROJECT_DIR"
    mvn clean package -DskipTests
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed!"
    echo ""
    echo "Please install Java 11 or higher:"
    echo ""
    echo "For macOS with Homebrew:"
    echo "  brew install openjdk@11"
    echo ""
    echo "For other systems, visit: https://www.java.com"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "✓ Java found: $JAVA_VERSION"
echo ""

# Check port availability
PORT=8080
if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Port $PORT is already in use."
    echo "Killing process on port $PORT..."
    lsof -ti:$PORT | xargs kill -9 || true
    sleep 2
fi

echo "Starting backend server..."
echo "Port: http://localhost:$PORT"
echo ""
echo "Logs will be written to: $PROJECT_DIR/logs/waste-management.log"
echo ""

# Start the application
java -Dspring.application.name="Attendance System" \
    -Dlogging.level.root=INFO \
    -Dlogging.file.name="$PROJECT_DIR/logs/attendance-system.log" \
    -jar "$JAR_FILE"

