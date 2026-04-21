# Project Setup and Installation Guide

## Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/Attendance-System.git
cd Attendance-System
```

### 2. Environment Setup

#### Windows
```batch
set JAVA_HOME=C:\Program Files\Java\jdk-11
set MAVEN_HOME=C:\apache-maven-3.8.1
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
```

#### macOS/Linux
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-11.0.x.jdk/Contents/Home
export MAVEN_HOME=/usr/local/apache-maven-3.8.1
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
```

### 3. Database Setup

#### Create Database
```bash
mysql -u root -p < sql/database_setup.sql
```

#### Verify Installation
```bash
mysql -u root -p -e "USE waste_management; SHOW TABLES;"
```

### 4. Update Configuration

Edit `src/main/resources/hibernate.cfg.xml`:
```xml
<!-- Update database credentials -->
<property name="hibernate.connection.url">
  jdbc:mysql://localhost:3306/waste_management?useSSL=false&serverTimezone=UTC
</property>
<property name="hibernate.connection.username">your_username</property>
<property name="hibernate.connection.password">your_password</property>
```

### 5. Build Project
```bash
mvn clean install
```

### 6. Run Application
```bash
mvn clean package
java -jar target/attendance-system-face-recognition-1.0.0.jar
```

## Project Structure

```
Attendance-System/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/waste/management/
│   │   │       ├── WasteManagementApplication.java       # Main entry point
│   │   │       ├── config/
│   │   │       │   └── HibernateConfig.java              # Database config
│   │   │       ├── entity/
│   │   │       │   ├── Worker.java                       # Worker entity
│   │   │       │   ├── WorkerRole.java                   # Role enum
│   │   │       │   ├── AttendanceRecord.java             # Attendance entity
│   │   │       │   ├── AttendanceStatus.java             # Status enum
│   │   │       │   ├── WasteTask.java                    # Task entity
│   │   │       │   └── TaskStatus.java                   # Task status enum
│   │   │       ├── repository/
│   │   │       │   ├── WorkerRepository.java             # Worker DB operations
│   │   │       │   ├── AttendanceRepository.java         # Attendance DB operations
│   │   │       │   └── WasteTaskRepository.java          # Task DB operations
│   │   │       ├── service/
│   │   │       │   ├── FaceRecognitionService.java       # Face recognition logic
│   │   │       │   ├── AttendanceService.java            # Attendance processing
│   │   │       │   └── WasteManagementService.java       # Waste task management
│   │   │       ├── dto/
│   │   │       │   ├── AttendanceDto.java                # Attendance DTO
│   │   │       │   ├── WasteTaskDto.java                 # Task DTO
│   │   │       │   └── ApiResponse.java                  # Response wrapper
│   │   │       └── util/
│   │   │           └── AppUtil.java                      # Utility methods
│   │   └── resources/
│   │       ├── hibernate.cfg.xml                         # Hibernate config
│   │       ├── log4j2.xml                                # Logging config
│   │       └── application.properties                    # App properties
│   └── test/
│       └── java/com/waste/management/
│           └── (Test classes)
├── sql/
│   └── database_setup.sql                                # Database schema
├── data/
│   └── faces/                                            # Facial data storage
├── logs/                                                 # Application logs
├── pom.xml                                               # Maven configuration
├── README.md                                             # Project readme
└── DOCUMENTATION.md                                      # Full documentation
```

## Dependency Installation

The project uses Maven to manage dependencies. All dependencies are defined in `pom.xml`.

### Key Dependencies
```xml
<!-- OpenCV for face recognition -->
<groupId>org.opencv</groupId>
<artifactId>opencv-java</artifactId>
<version>4.8.0</version>

<!-- Hibernate for ORM -->
<groupId>org.hibernate</groupId>
<artifactId>hibernate-core</artifactId>
<version>5.6.15.Final</version>

<!-- MySQL driver -->
<groupId>mysql</groupId>
<artifactId>mysql-connector-java</artifactId>
<version>8.0.33</version>
```

### Install Dependencies
```bash
mvn dependency:resolve
mvn dependency:tree
```

## Configuration Files

### Hibernate Configuration (hibernate.cfg.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC>
<hibernate-configuration>
    <session-factory>
        <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/waste_management</property>
        <property name="hibernate.connection.username">root</property>
        <property name="hibernate.connection.password">password</property>
        <property name="hibernate.hbm2ddl.auto">update</property>
        <property name="hibernate.show_sql">true</property>
    </session-factory>
</hibernate-configuration>
```

### Logging Configuration (log4j2.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{ISO8601} [%t] %-5p %c{1} - %msg%n"/>
        </Console>
        <File name="File" fileName="logs/waste-management.log">
            <PatternLayout pattern="%d{ISO8601} [%t] %-5p %c{1} - %msg%n"/>
        </File>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="File"/>
        </Root>
    </Loggers>
</Configuration>
```

## Running the Application

### Option 1: Direct Execution
```bash
java -cp target/attendance-system-face-recognition-1.0.0.jar com.waste.management.WasteManagementApplication
```

### Option 2: Maven Execution
```bash
mvn clean package
mvn exec:java -Dexec.mainClass="com.waste.management.WasteManagementApplication"
```

### Option 3: Docker Container
```bash
docker build -t waste-management:1.0 .
docker run -p 8080:8080 waste-management:1.0
```

## Troubleshooting

### Common Issues and Solutions

#### Issue: "Cannot find OpenCV library"
**Solution:**
```bash
# Verify OpenCV installation
mvn dependency:tree | grep opencv

# Re-download dependencies
mvn clean install -U
```

#### Issue: "Database connection refused"
**Solution:**
```bash
# Check MySQL is running
ps aux | grep mysql

# Verify connection string in hibernate.cfg.xml
# Format: jdbc:mysql://hostname:port/database

# Test connection
mysql -h localhost -u root -p waste_management
```

#### Issue: "No faces detected"
**Solution:**
- Ensure good lighting conditions
- Keep face frontal to camera
- Update reference face image quality
- Check image file path is correct

#### Issue: "Port already in use"
**Solution:**
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Run Specific Test Class
```bash
mvn test -Dtest=FaceRecognitionServiceTest
```

## IDE Setup

### IntelliJ IDEA
1. Open project: File → Open → Select project folder
2. Configure SDK: File → Project Structure → Project → SDK → Java 11
3. Configure Database: Tools → Database → New Data Source → MySQL
4. Maven: View → Tool Windows → Maven → Enable Auto-Import

### Eclipse
1. Import project: File → Import → Existing Maven Projects
2. Configure SDK: Project Properties → Java Compiler → Java Version 11
3. Add Database Driver: Project Properties → Libraries → Add External Jar

### VS Code
1. Install extensions: Maven for Java, Language Support for Java
2. Open folder and allow Maven import
3. Create launch.json for debugging configuration

## Performance Tuning

### Database Optimization
```sql
-- Add indexes for better query performance
CREATE INDEX idx_worker_active ON workers(active);
CREATE INDEX idx_attendance_date ON attendance_records(DATE(check_in_time));
CREATE INDEX idx_task_status ON waste_tasks(status);
```

### Memory Configuration
```bash
# Allocate more memory
java -Xmx1024m -Xms512m -jar target/attendance-system-*.jar
```

### Connection Pooling
```xml
<!-- Configure in hibernate.cfg.xml -->
<property name="hibernate.connection.pool_size">20</property>
```

## Production Deployment

### Environment Variables
```bash
export DB_HOST=mysql.example.com
export DB_USER=prod_user
export DB_PASSWORD=secure_password
export APP_LOG_LEVEL=INFO
```

### Database Backup
```bash
mysqldump -u root -p waste_management > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Health Check
```bash
curl http://localhost:8080/api/health
```

## Next Steps

1. **Customize Configuration**: Update database credentials and settings
2. **Register Workers**: Add worker data with facial recognition data
3. **Test Face Recognition**: Verify with sample images
4. **Setup Monitoring**: Configure logging and alerting
5. **Schedule Backups**: Implement automated database backups
6. **Deploy to Production**: Follow deployment guidelines

## Additional Resources

- [OpenCV Java Documentation](https://docs.opencv.org/java/)
- [Hibernate Documentation](https://hibernate.org/orm/documentation/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Maven Documentation](https://maven.apache.org/guides/)

## Support

For issues or questions:
- Check DOCUMENTATION.md for detailed information
- Review error logs in `logs/waste-management.log`
- Create GitHub issue with error details
- Contact: support@waste-management.com
