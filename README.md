# Attendance System with Face Recognition

A modern, full-stack attendance management system with integrated face recognition capabilities for automated worker check-in and check-out operations.

## 🎯 Features

### Core Attendance Features
- **Check-In/Check-Out**: Seamless worker attendance tracking with timestamps
- **Face Recognition**: Automated facial identification for quick attendance marking
- **Live Camera Integration**: Real-time face detection and recognition from webcam
- **Attendance Reports**: Comprehensive analytics and attendance history
- **Worker Management**: Complete worker database management

### Technical Features
- **Real-time Updates**: Live camera feeds and instant attendance updates
- **Location Tracking**: Geo-location data for attendance verification
- **Secure API**: RESTful API with proper authentication and authorization
- **Responsive UI**: Mobile-friendly frontend interface
- **Database Management**: Robust data persistence with multiple database support

## 📋 Prerequisites

- **Java 11+** (for backend)
- **Node.js 14+** (for frontend)
- **Maven 3.6+** (for building)
- **MySQL/H2 Database**
- **npm or yarn** (for frontend dependencies)

## 🚀 Quick Start

### Backend Setup

1. **Navigate to the backend directory**
   ```bash
   cd backend
   ```

2. **Install dependencies and build**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   
   The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to the frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```
   
   The frontend will start on `http://localhost:3000`

## 📁 Project Structure

```
Attendance-System/
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java source code
│   │   │   └── resources/     # Configuration files
│   │   └── test/              # Unit tests
│   ├── pom.xml                # Maven configuration
│   └── README.md              # Backend documentation
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── components/        # React components
│   │   ├── pages/            # Page components
│   │   ├── services/         # API services
│   │   └── styles/           # CSS stylesheets
│   ├── package.json          # NPM configuration
│   └── README.md             # Frontend documentation
├── sql/                        # Database setup scripts
├── data/                       # Data files (face data, etc.)
├── uploads/                    # Upload directory for images
├── docker-compose.yml          # Docker compose configuration
├── Dockerfile                  # Docker configuration
└── pom.xml                     # Parent Maven configuration
```

## 🔧 Configuration

### Database Configuration
Configure your database in `backend/src/main/resources/application.properties`:

```properties
# Database configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

For MySQL:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attendance_db
spring.datasource.username=root
spring.datasource.password=password
```

### API Configuration
- **Base URL**: `http://localhost:8080/api`
- **Endpoints**: See `API_DOCUMENTATION.md` for complete API reference

## 🌐 API Endpoints

### Workers
- `GET /api/workers` - Get all workers
- `GET /api/workers/{id}` - Get worker by ID
- `POST /api/workers` - Create new worker
- `PUT /api/workers/{id}` - Update worker
- `DELETE /api/workers/{id}` - Delete worker

### Attendance
- `POST /api/attendance/check-in` - Mark check-in
- `POST /api/attendance/check-out` - Mark check-out
- `GET /api/attendance/report` - Get attendance report
- `GET /api/attendance/history/{workerId}` - Get worker history

### Face Recognition
- `POST /api/face-recognition/register` - Register face
- `POST /api/face-recognition/recognize` - Recognize face

## 🐳 Docker Support

Build and run using Docker:

```bash
docker-compose up --build
```

This will start:
- Backend service on port 8080
- Frontend service on port 3000
- Database service

## 📊 Database Setup

Run the database setup script:

```bash
mysql -u root -p < sql/database_setup.sql
```

Or for H2 (embedded):
```bash
mvn spring-boot:run
```

## 🧪 Testing

### Run Backend Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=YourTestClass
```

### Run Frontend Tests
```bash
cd frontend
npm test
```

## 🔐 Security Features

- Input validation and sanitization
- Secure API endpoints with authentication
- CORS configuration for frontend integration
- Password encryption for user credentials
- Session management

## 📝 Logging

Logs are configured in `src/main/resources/log4j2.xml`:
- **Console Logging**: Real-time log output
- **File Logging**: Persistent logs in `logs/` directory
- **Log Levels**: INFO, DEBUG, ERROR, WARN

View logs:
```bash
tail -f logs/waste-management.log
```

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## 📚 Documentation

- `START_HERE.md` - Getting started guide
- `API_DOCUMENTATION.md` - Complete API reference
- `SETUP_GUIDE.md` - Detailed setup instructions
- `FACE_RECOGNITION_GUIDE.md` - Face recognition setup
- `QUICK_REFERENCE.md` - Quick reference guide

## 🐛 Troubleshooting

### Backend won't start
- Check if port 8080 is already in use
- Verify Maven installation: `mvn -v`
- Check database connection settings

### Frontend won't start
- Clear npm cache: `npm cache clean --force`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`
- Check if port 3000 is available

### Face Recognition not working
- Ensure camera permissions are granted
- Check face detection model is loaded
- Verify image quality and lighting

## 📱 System Requirements

- **Minimum RAM**: 4GB
- **Disk Space**: 2GB free space
- **Recommended RAM**: 8GB+
- **Browser Support**: Chrome, Firefox, Safari (latest versions)

## 📧 Support

For issues, questions, or suggestions, please contact the development team or create an issue in the repository.

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

## 🎓 Technology Stack

### Backend
- **Framework**: Spring Boot 2.x
- **ORM**: Hibernate/JPA
- **Database**: H2, MySQL
- **Build Tool**: Maven
- **Face Recognition**: OpenCV, Deep Learning models
- **Logging**: Log4j2

### Frontend
- **Framework**: React
- **State Management**: React Hooks
- **HTTP Client**: Axios
- **Styling**: CSS3, Bootstrap
- **Camera Integration**: WebRTC, navigator.mediaDevices
- **Face Detection**: face-api.js, TensorFlow.js

## 🎯 Roadmap

- [ ] Mobile app (iOS/Android)
- [ ] Advanced analytics and reporting
- [ ] Multi-location support
- [ ] Integration with payroll systems
- [ ] Biometric authentication (fingerprint, iris)
- [ ] Voice recognition integration

---

