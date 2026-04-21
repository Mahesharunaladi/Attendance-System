#!/bin/bash

# Waste Management Attendance System - Build and Deploy Script
# This script builds and deploys the Java application

set -e

PROJECT_NAME="Attendance System - Face Recognition"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$PROJECT_DIR/target"
LOG_DIR="$PROJECT_DIR/logs"
DATA_DIR="$PROJECT_DIR/data"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}$PROJECT_NAME${NC}"
echo -e "${BLUE}Build and Deployment Script${NC}"
echo -e "${BLUE}========================================${NC}"

# Function to print colored messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        exit 1
    fi
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "Found: $JAVA_VERSION"

    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed"
        exit 1
    fi
    MVN_VERSION=$(mvn -version 2>&1 | head -n 1)
    print_success "Found: $MVN_VERSION"

    # Check MySQL (optional)
    if command -v mysql &> /dev/null; then
        print_success "MySQL is installed"
    else
        print_warning "MySQL is not installed (needed for database setup)"
    fi
}

# Create necessary directories
create_directories() {
    print_info "Creating necessary directories..."

    mkdir -p "$LOG_DIR"
    mkdir -p "$DATA_DIR/faces"
    mkdir -p "$BUILD_DIR"

    print_success "Directories created"
}

# Build project
build_project() {
    print_info "Building project..."

    cd "$PROJECT_DIR"
    mvn clean package -DskipTests

    print_success "Project built successfully"
}

# Setup database
setup_database() {
    print_info "Setting up database..."

    if command -v mysql &> /dev/null; then
        read -p "Enter MySQL root password (or press Enter to skip): " -s DB_PASSWORD
        echo ""

        if [ -z "$DB_PASSWORD" ]; then
            mysql -u root < sql/database_setup.sql
        else
            mysql -u root -p"$DB_PASSWORD" < sql/database_setup.sql
        fi

        print_success "Database setup completed"
    else
        print_warning "MySQL not found. Please run SQL setup manually:"
        echo "mysql -u root -p < $PROJECT_DIR/sql/database_setup.sql"
    fi
}

# Run application
run_application() {
    print_info "Starting application..."

    JAR_FILE=$(find "$BUILD_DIR" -name "*.jar" -type f | head -n 1)

    if [ -z "$JAR_FILE" ]; then
        print_error "JAR file not found"
        exit 1
    fi

    print_success "Found JAR: $JAR_FILE"

    java -Xmx512m -Xms256m -jar "$JAR_FILE"
}

# Docker deployment
docker_deploy() {
    print_info "Building Docker image..."

    cd "$PROJECT_DIR"
    docker build -t waste-management:1.0 .

    print_success "Docker image built"

    print_info "Starting Docker containers..."
    docker-compose up -d

    print_success "Docker containers started"
    echo -e "${BLUE}Application is available at: http://localhost:8080${NC}"
}

# Display usage
show_usage() {
    echo ""
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  build           - Build the project"
    echo "  setup           - Setup database"
    echo "  run             - Run the application"
    echo "  docker          - Build and run with Docker"
    echo "  clean           - Clean build artifacts"
    echo "  all             - Check prerequisites, build, setup DB, and run"
    echo "  help            - Show this help message"
    echo ""
}

# Main script logic
case "${1:-all}" in
    build)
        check_prerequisites
        create_directories
        build_project
        ;;
    setup)
        setup_database
        ;;
    run)
        create_directories
        run_application
        ;;
    docker)
        create_directories
        docker_deploy
        ;;
    clean)
        print_info "Cleaning build artifacts..."
        cd "$PROJECT_DIR"
        mvn clean
        rm -rf "$BUILD_DIR"
        print_success "Cleaned"
        ;;
    all)
        check_prerequisites
        create_directories
        build_project
        setup_database
        run_application
        ;;
    help)
        show_usage
        ;;
    *)
        print_error "Unknown option: $1"
        show_usage
        exit 1
        ;;
esac

print_success "Operation completed successfully!"
