# Waste Management Attendance System with Face Recognition

## Overview

A comprehensive Java-based attendance and waste management system that uses **face recognition technology** to automatically track attendance of cleaner, driver, and helper personnel in waste management operations. The system combines biometric verification with waste task management and GPS tracking.

## Features

### 👤 Face Recognition & Attendance
- **Automated Face Recognition**: Using OpenCV and histogram-based comparison for worker identification
- **Check-In/Check-Out System**: Real-time face verification with confidence scoring
- **GPS Location Tracking**: Capture worker location during attendance events
- **Attendance Status**: Automatically classify as Present, Late, Absent, or On Duty
- **Multiple Face Detection**: Support for detecting and verifying multiple workers in single image

### 👨‍💼 Worker Management
- **Role-Based Management**: Support for Cleaner, Driver, Helper, Supervisor, and Manager roles
- **Worker Registration**: Store facial data along with personal information
- **Department Assignment**: Organize workers by department
- **Active/Inactive Status**: Manage worker availability

### 🗑️ Waste Management
- **Task Assignment**: Create and assign waste collection tasks
- **Worker Assignment**: Assign cleaners and helpers to specific collection areas
- **Driver Management**: Track vehicle drivers for waste transportation
- **Weight Tracking**: Record estimated and actual waste weight
- **GPS Coordinates**: Log collection location for audit trail
- **Task Status**: Track task progress (Pending → In Progress → Completed)

### 📊 Reporting & Analytics
- **Daily Attendance Reports**: View present/absent counts
- **Attendance Rate Calculation**: Percentage-based metrics
- **Waste Collection Statistics**: Total weight collected and task completion rates
- **Worker-Specific Reports**: Individual attendance history
- **Date Range Reports**: Filter records by period

## System Architecture

```
┌─────────────────────────────────────────┐
│   Face Recognition Service              │
│   (OpenCV Integration)                  │
└────────────┬────────────────────────────┘
             │
┌────────────┴────────────────────────────┐
│   Attendance Service                    │
│   - Check-in/Check-out Processing       │
│   - Status Determination                │
└────────────┬────────────────────────────┘
             │
┌────────────┴────────────────────────────┐
│   Waste Management Service              │
│   - Task Management                     │
│   - Worker Assignment                   │
│   - Statistics & Reporting              │
└────────────┬────────────────────────────┘
             │
┌────────────┴────────────────────────────┐
│   Repository Layer (Hibernate/JPA)      │
│   - Worker Repository                   │
│   - Attendance Repository               │
│   - Waste Task Repository               │
└────────────┬────────────────────────────┘
             │
┌────────────┴────────────────────────────┐
│   MySQL Database                        │
│   - workers table                       │
│   - attendance_records table            │
│   - waste_tasks table                   │
└─────────────────────────────────────────┘
```

## Tech Stack

### Core Technologies
- **Language**: Java 11+
- **Build Tool**: Maven
- **ORM**: Hibernate 5.6
- **Database**: MySQL 8.0

### Key Libraries
- **Face Recognition**: OpenCV 4.8.0
- **Image Processing**: OpenCV Java bindings
- **Logging**: SLF4J + Logback
- **JSON**: Gson 2.10.1
- **Database Driver**: MySQL Connector Java 8.0

## Installation & Setup

### Prerequisites
- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6+
- Webcam/Camera device (for live face capture)

### Step 1: Database Setup
```bash
# Create database and tables
mysql -u root -p < sql/database_setup.sql
```

### Step 2: Configure Database Connection
Edit `src/main/resources/hibernate.cfg.xml`:
```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/waste_management</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">your_password</property>
```

### Step 3: Install Dependencies
```bash
mvn clean install
```

### Step 4: Build the Project
```bash
mvn clean package
```

### Step 5: Run the Application
```bash
java -cp target/attendance-system-face-recognition-1.0.0.jar com.waste.management.WasteManagementApplication
```

## Database Schema

### Workers Table
| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| employee_id | VARCHAR(50) | Unique employee ID |
| full_name | VARCHAR(100) | Worker name |
| email | VARCHAR(100) | Email address |
| phone_number | VARCHAR(20) | Contact number |
| role | ENUM | CLEANER/DRIVER/HELPER/SUPERVISOR/MANAGER |
| facial_data_path | VARCHAR(255) | Path to stored face image |
| department | VARCHAR(100) | Department assignment |
| active | BOOLEAN | Worker status |

### Attendance Records Table
| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| worker_id | BIGINT | Foreign key to workers |
| check_in_time | TIMESTAMP | Time of check-in |
| check_out_time | TIMESTAMP | Time of check-out |
| status | ENUM | PRESENT/ABSENT/LATE/EARLY_LEAVE/ON_DUTY |
| face_match_confidence | DOUBLE | Face recognition confidence score |
| location_latitude | DOUBLE | GPS latitude |
| location_longitude | DOUBLE | GPS longitude |

### Waste Tasks Table
| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| task_id | VARCHAR(50) | Unique task identifier |
| area | VARCHAR(255) | Collection area |
| waste_type | VARCHAR(100) | Type of waste |
| estimated_weight | DOUBLE | Expected weight (kg) |
| actual_weight | DOUBLE | Actual collected weight (kg) |
| status | ENUM | PENDING/IN_PROGRESS/COMPLETED/CANCELLED |
| assigned_to_worker_id | BIGINT | Assigned cleaner/helper |
| driver_id | BIGINT | Assigned vehicle driver |

## Usage Examples

### 1. Register a Worker
```java
WasteManagementApplication app = new WasteManagementApplication();
app.registerWorker(
    "EMP001",                    // Employee ID
    "John Smith",                // Name
    "john@waste.com",            // Email
    "9876543210",                // Phone
    WorkerRole.CLEANER,          // Role
    "data/faces/john_face.jpg"   // Facial data path
);
```

### 2. Process Check-In
```java
app.processCheckIn(
    "EMP001",                      // Employee ID
    "path/to/captured_image.jpg",  // Captured image from camera
    12.9716,                       // Latitude
    77.5946                        // Longitude
);
```

### 3. Create Waste Collection Task
```java
WasteTask task = wasteManagementService.createWasteTask(
    "Downtown Area A",     // Area
    "Organic Waste",       // Waste type
    75.5                   // Estimated weight in kg
);
```

### 4. Assign Worker to Task
```java
wasteManagementService.assignWorkerToTask(task.getId(), cleanerWorker);
```

### 5. Complete Task
```java
wasteManagementService.completeTask(
    taskId,        // Task ID
    82.3,          // Actual weight collected
    12.9716,       // Latitude
    77.5946        // Longitude
);
```

### 6. Get Reports
```java
// Daily attendance statistics
app.displayTodayStatistics();

// Waste management statistics
app.displayWasteStatistics();

// Get attendance report for date range
List<AttendanceRecord> records = attendanceService.getAttendanceReport(
    LocalDate.of(2026, 4, 1),
    LocalDate.of(2026, 4, 30)
);

// Get worker-specific report
List<AttendanceRecord> workerRecords = attendanceService.getWorkerAttendanceReport(
    workerId,
    LocalDate.of(2026, 4, 1),
    LocalDate.of(2026, 4, 30)
);
```

## Face Recognition Details

### How It Works
1. **Image Capture**: Capture image from camera or file
2. **Face Detection**: Detect face regions using Haar Cascade Classifier
3. **Feature Extraction**: Extract facial features from detected regions
4. **Histogram Comparison**: Compare histogram of captured face with stored reference
5. **Confidence Scoring**: Calculate similarity score (0.0 to 1.0)
6. **Threshold Matching**: Match if confidence ≥ 0.75 (configurable)

### Configuration
```java
// Match threshold in FaceRecognitionService.java
private static final double MATCH_THRESHOLD = 0.75; // Confidence score threshold
```

### Supported Image Formats
- JPG/JPEG
- PNG
- BMP
- TIFF

## Performance Considerations

### Optimization Tips
1. **Image Resolution**: Use 224x224 pixel images for optimal face recognition
2. **Database Indexing**: Indexes on frequently queried fields (employee_id, status, dates)
3. **Connection Pooling**: Hibernate configured with pool size of 10
4. **Query Performance**: Use date-range indexes for attendance reports

### Scalability
- Supports thousands of workers
- Efficient date-range queries for reports
- Batch processing support for task assignment

## Security Features

### Data Protection
- Database credentials in configuration file (should use environment variables)
- Facial data stored as file paths with restricted access
- Attendance records linked to worker ID
- Timestamp tracking for audit trail

### Recommendations
- Use environment variables for database credentials
- Implement role-based access control (RBAC)
- Encrypt sensitive data in database
- Use HTTPS for API endpoints (if REST service added)
- Validate all input data

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Sample Test Case
```java
@Test
public void testFaceRecognition() {
    FaceRecognitionService service = new FaceRecognitionService();
    double similarity = service.compareFaces(
        "path/to/reference_face.jpg",
        "path/to/test_face.jpg"
    );
    assertTrue(similarity >= 0.75);
}
```

## Troubleshooting

### Common Issues

**1. OpenCV Library Not Found**
```
Solution: Ensure OpenCV Java bindings are in classpath
mvn dependency:tree | grep opencv
```

**2. Database Connection Failed**
```
Solution: Verify MySQL is running and credentials are correct
mysql -u root -p -e "SELECT VERSION();"
```

**3. Face Recognition Accuracy Low**
```
Solutions:
- Ensure good lighting conditions
- Keep face frontal to camera
- Verify facial data reference image quality
- Adjust MATCH_THRESHOLD value
```

**4. Permission Denied for Facial Data Path**
```
Solution: Ensure write permissions in data/faces directory
chmod -R 755 data/faces/
```

## Deployment

### Docker Deployment
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/attendance-system-face-recognition-1.0.0.jar app.jar
ENV JAVA_OPTS="-Xmx512m -Xms256m"
CMD ["java", "$JAVA_OPTS", "-jar", "app.jar"]
```

### Configuration for Production
1. Use environment variables for database credentials
2. Enable SSL for database connections
3. Implement proper logging and monitoring
4. Use connection pooling
5. Regular database backups

## Future Enhancements

- [ ] REST API endpoints for remote access
- [ ] Web UI dashboard with real-time analytics
- [ ] Mobile app for workers
- [ ] Advanced ML models for face recognition (DeepFace, FaceNet)
- [ ] Multi-factor authentication
- [ ] Geofencing for location verification
- [ ] Integration with payment systems
- [ ] SMS/Email notifications
- [ ] Data export to Excel/PDF
- [ ] Cloud deployment options

## Contributing

Guidelines for contributing to the project:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support & Documentation

- **Issue Tracker**: GitHub Issues
- **Documentation**: See `/docs` directory
- **API Reference**: See Javadoc in source code
- **FAQ**: Check wiki pages

## Contact

For questions and support:
- Email: support@waste-management.com
- Issues: GitHub Issues
- Documentation: Project Wiki

---

**Last Updated**: April 21, 2026
**Version**: 1.0.0
**Status**: Production Ready
