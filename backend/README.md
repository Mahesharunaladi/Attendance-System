# Attendance System Backend

Spring Boot REST API backend for the Waste Management Attendance System with Face Recognition.

## Features

- Face recognition using OpenCV
- Attendance tracking with GPS location
- Waste management task assignment and tracking
- Worker management
- Attendance reports and statistics
- Hibernate ORM with MySQL database
- RESTful API endpoints

## Prerequisites

- Java 11 or higher
- Maven 3.8+
- MySQL 8.0+
- OpenCV 4.6.0

## Setup Instructions

### 1. Database Setup

Create MySQL database:
```bash
mysql -u root -p
CREATE DATABASE attendance_system;
USE attendance_system;
```

Import the schema (if using SQL setup file):
```bash
mysql -u root -p attendance_system < database_setup.sql
```

### 2. Backend Configuration

1. Navigate to backend directory:
```bash
cd backend
```

2. Update database credentials in `src/main/resources/application.properties` and `src/main/resources/hibernate.cfg.xml` if needed.

### 3. Build and Run

Build the project:
```bash
mvn clean install
```

Run the application:
```bash
mvn spring-boot:run
```

Or run the JAR file:
```bash
java -jar target/attendance-system-backend-1.0.0.jar
```

The backend will start at `http://localhost:8080`

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/waste/management/
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── repository/      # Data Access Layer
│   │   │   ├── service/         # Business Logic
│   │   │   ├── controller/      # REST Endpoints
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── util/            # Utility Classes
│   │   │   ├── config/          # Configuration Classes
│   │   │   └── exception/       # Custom Exceptions
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── hibernate-h2.cfg.xml
│   │       ├── hibernate.cfg.xml
│   │       └── logback.xml
│   └── test/
├── pom.xml
└── README.md
```

## API Endpoints

### Attendance API
- `POST /api/attendance/checkin` - Record employee check-in
- `POST /api/attendance/checkout` - Record employee check-out
- `GET /api/attendance/today` - Get today's attendance status
- `GET /api/attendance/report` - Get attendance report (date range)

### Workers API
- `POST /api/workers/register` - Register new worker
- `GET /api/workers` - Get all active workers
- `GET /api/workers/{id}` - Get specific worker
- `GET /api/workers/role/{role}` - Get workers by role
- `PUT /api/workers/{id}` - Update worker
- `PUT /api/workers/{id}/deactivate` - Deactivate worker

### Waste Tasks API
- `POST /api/waste-tasks/create` - Create new task
- `GET /api/waste-tasks/pending` - Get pending tasks
- `GET /api/waste-tasks/in-progress` - Get in-progress tasks
- `POST /api/waste-tasks/{id}/assign-worker` - Assign worker to task
- `POST /api/waste-tasks/{id}/assign-driver` - Assign driver to task
- `POST /api/waste-tasks/{id}/complete` - Complete task
- `POST /api/waste-tasks/{id}/cancel` - Cancel task

## Technologies

- **Framework**: Spring Boot 3.1.0
- **Language**: Java 11
- **ORM**: Hibernate 5.6.15
- **Database**: MySQL 8.0
- **Computer Vision**: OpenCV 4.6.0
- **Build Tool**: Maven 3.8+
- **Logging**: SLF4J + Logback

## Configuration

### Face Recognition Settings
- Threshold: 0.75 (adjustable in `application.properties`)
- Model path: `models/face_recognition_model`
- Image storage: `uploads/faces`

### Attendance System
- Working hours: 09:00 - 18:00
- Late threshold: 09:15
- GPS accuracy: 100 meters

## Error Handling

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "code": 400,
  "data": null
}
```

## Logging

Logs are stored in:
- Console output (INFO and above)
- File: `logs/attendance-system.log`
- Daily rotation with 30-day retention

## Testing

Run unit tests:
```bash
mvn test
```

## Docker Deployment

Build Docker image:
```bash
docker build -t attendance-system-backend .
```

Run with Docker Compose:
```bash
docker-compose up -d
```

## Security Notes

- Always update database credentials in production
- Use environment variables for sensitive configuration
- Enable HTTPS in production
- Implement API authentication (JWT recommended)
- Validate and sanitize all inputs

## Troubleshooting

### Missing OpenCV artifact
Update to valid OpenCV version (4.6.0) in pom.xml

### Database connection error
Check MySQL credentials and ensure database exists

### Face recognition not working
Verify OpenCV native libraries are properly loaded

## Support

For issues and support, please refer to the documentation or create an issue in the repository.
