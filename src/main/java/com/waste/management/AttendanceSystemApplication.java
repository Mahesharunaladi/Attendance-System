package com.waste.management;

import com.waste.management.config.HibernateConfig;
import com.waste.management.entity.Worker;
import com.waste.management.entity.WorkerRole;
import com.waste.management.entity.Gender;
import com.waste.management.repository.*;
import com.waste.management.service.AttendanceService;
import com.waste.management.service.FaceRecognitionService;
import com.waste.management.service.WasteManagementService;
import org.hibernate.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Boot Application entry point for Attendance System
 * Initializes workers and provides REST API endpoints
 */
@SpringBootApplication
public class AttendanceSystemApplication {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceSystemApplication.class);
    
    private WorkerRepository workerRepository;
    private AttendanceRepository attendanceRepository;
    private WasteTaskRepository wasteTaskRepository;
    private FaceRecognitionService faceRecognitionService;
    
    private static boolean initialized = false;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeWorkers() {
        if (initialized) {
            return;
        }
        initialized = true;
        
        logger.info("Initializing workers...");
        
        try {
            SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
            this.workerRepository = new WorkerRepository(sessionFactory);
            this.attendanceRepository = new AttendanceRepository(sessionFactory);
            this.wasteTaskRepository = new WasteTaskRepository(sessionFactory);
            this.faceRecognitionService = new FaceRecognitionService();
            new AttendanceService(attendanceRepository, faceRecognitionService, workerRepository);
            new WasteManagementService(wasteTaskRepository);
            
            // Check if workers already exist
            List<Worker> existingWorkers = workerRepository.findAllActive();
            if (existingWorkers != null && !existingWorkers.isEmpty()) {
                logger.info("Workers already initialized. Count: {}", existingWorkers.size());
                for (Worker w : existingWorkers) {
                    logger.info("  ✓ {}: {} ({})", w.getEmployeeId(), w.getFullName(), w.getRole().getDisplayName());
                }
                return;
            }
            
            // Register workers with complete details
            initializeWorker("EMP001", "John Cleaner", "john@waste.com", "9876543210", 
                           WorkerRole.CLEANER, "data/faces/john_face.jpg",
                           "123456789012", "MALE", "General");
            
            initializeWorker("EMP002", "Mike Driver", "mike@waste.com", "9876543211", 
                           WorkerRole.DRIVER, "data/faces/mike_face.jpg",
                           "123456789013", "MALE", "General");
            
            initializeWorker("EMP003", "Sarah Helper", "sarah@waste.com", "9876543212", 
                           WorkerRole.HELPER, "data/faces/sarah_face.jpg",
                           "123456789014", "FEMALE", "OBC");
            
            initializeWorker("EMP004", "Rajesh Supervisor", "rajesh@waste.com", "9876543213", 
                           WorkerRole.SUPERVISOR, "data/faces/rajesh_face.jpg",
                           "123456789015", "MALE", "SC");
            
            initializeWorker("EMP005", "Priya Manager", "priya@waste.com", "9876543214", 
                           WorkerRole.MANAGER, "data/faces/priya_face.jpg",
                           "123456789016", "FEMALE", "General");
            
            // Amit Worker with facial data from user's photo
            initializeWorker("EMP006", "Amit Worker", "amit@waste.com", "9876543215", 
                           WorkerRole.CLEANER, "data/faces/worker_amit_face.txt",
                           "123456789017", "MALE", "ST");
            
            initializeWorker("EMP007", "Neha Driver", "neha@waste.com", "9876543216", 
                           WorkerRole.DRIVER, "data/faces/neha_face.jpg",
                           "123456789018", "FEMALE", "General");
            
            logger.info("✓ All workers initialized successfully! Total: 7");
            logger.info("✓ Facial recognition system ready");
            logger.info("✓ Backend API endpoints available at http://localhost:8080");
        } catch (Exception e) {
            logger.error("Error initializing workers", e);
        }
    }
    
    private void initializeWorker(String employeeId, String fullName, String email,
                                 String phoneNumber, WorkerRole role, String facialDataPath,
                                 String aadharNumber, String gender, String caste) {
        try {
            Worker worker = new Worker();
            worker.setEmployeeId(employeeId);
            worker.setFullName(fullName);
            worker.setEmail(email);
            worker.setPhoneNumber(phoneNumber);
            worker.setRole(role);
            worker.setFacialDataPath(facialDataPath);
            worker.setDepartment("Waste Management");
            worker.setActive(true);
            worker.setCreatedAt(LocalDateTime.now());
            worker.setAadharNumber(aadharNumber);
            if (gender != null) {
                worker.setGender(Gender.valueOf(gender.toUpperCase()));
            }
            worker.setCaste(caste);
            
            workerRepository.save(worker);
            logger.info("  ✓ {} ({}) - Facial Data: {}", fullName, employeeId, facialDataPath);
        } catch (Exception e) {
            logger.error("Error registering worker {}", employeeId, e);
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Attendance System Application...");
        SpringApplication.run(AttendanceSystemApplication.class, args);
        logger.info("╔═══════════════════════════════════════╗");
        logger.info("║  Attendance System - Ready!           ║");
        logger.info("║  Backend: http://localhost:8080       ║");
        logger.info("║  Frontend: http://localhost:3000      ║");
        logger.info("╚═══════════════════════════════════════╝");
    }
}
