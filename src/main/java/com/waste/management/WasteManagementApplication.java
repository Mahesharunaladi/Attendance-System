package com.waste.management;

import com.waste.management.config.HibernateConfig;
import com.waste.management.entity.AttendanceStatus;
import com.waste.management.entity.Worker;
import com.waste.management.entity.WorkerRole;
import com.waste.management.repository.AttendanceRepository;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.repository.WasteTaskRepository;
import com.waste.management.service.AttendanceService;
import com.waste.management.service.FaceRecognitionService;
import com.waste.management.service.WasteManagementService;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Main Application class for Waste Management Attendance System
 * with Face Recognition capability
 */
public class WasteManagementApplication {
    private static final Logger logger = LoggerFactory.getLogger(WasteManagementApplication.class);

    private final WorkerRepository workerRepository;
    private final AttendanceRepository attendanceRepository;
    private final WasteTaskRepository wasteTaskRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final AttendanceService attendanceService;
    private final WasteManagementService wasteManagementService;

    public WasteManagementApplication() {
        SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
        
        this.workerRepository = new WorkerRepository(sessionFactory);
        this.attendanceRepository = new AttendanceRepository(sessionFactory);
        this.wasteTaskRepository = new WasteTaskRepository(sessionFactory);
        this.faceRecognitionService = new FaceRecognitionService();
        this.attendanceService = new AttendanceService(attendanceRepository, faceRecognitionService);
        this.wasteManagementService = new WasteManagementService(wasteTaskRepository);
    }

    /**
     * Register a new worker with facial data
     */
    public void registerWorker(String employeeId, String fullName, String email, 
                              String phoneNumber, WorkerRole role, String facialDataPath) {
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

        Worker savedWorker = workerRepository.save(worker);
        logger.info("Worker registered successfully: {} ({})", fullName, employeeId);
    }

    /**
     * Process attendance check-in via face recognition
     */
    public void processCheckIn(String employeeId, String capturedImagePath, 
                              Double latitude, Double longitude) {
        Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
        
        if (worker.isEmpty()) {
            logger.warn("Worker not found: {}", employeeId);
            return;
        }

        Optional<?> result = attendanceService.recordCheckIn(worker.get(), capturedImagePath, latitude, longitude);
        if (result.isPresent()) {
            logger.info("Check-in successful for: {}", employeeId);
        } else {
            logger.warn("Check-in failed for: {}", employeeId);
        }
    }

    /**
     * Process attendance check-out via face recognition
     */
    public void processCheckOut(String employeeId, String capturedImagePath, 
                               Double latitude, Double longitude) {
        Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
        
        if (worker.isEmpty()) {
            logger.warn("Worker not found: {}", employeeId);
            return;
        }

        Optional<?> result = attendanceService.recordCheckOut(worker.get(), capturedImagePath, latitude, longitude);
        if (result.isPresent()) {
            logger.info("Check-out successful for: {}", employeeId);
        } else {
            logger.warn("Check-out failed for: {}", employeeId);
        }
    }

    /**
     * Get today's attendance statistics
     */
    public void displayTodayStatistics() {
        List<Worker> allWorkers = workerRepository.findAllActive();
        long presentCount = attendanceService.getTodayAttendanceCount();
        long absentCount = attendanceService.getAbsenteeCount(allWorkers, 
                                                             java.time.LocalDate.now());

        logger.info("=== Today's Attendance Statistics ===");
        logger.info("Total Active Workers: {}", allWorkers.size());
        logger.info("Present: {}", presentCount);
        logger.info("Absent: {}", absentCount);
        logger.info("Attendance Rate: {:.2f}%", (presentCount * 100.0 / allWorkers.size()));
    }

    /**
     * Display waste management statistics
     */
    public void displayWasteStatistics() {
        long[] stats = wasteManagementService.getCompletionStatistics();
        Double totalWaste = wasteManagementService.getTotalWasteCollected();

        logger.info("=== Waste Management Statistics ===");
        logger.info("Total Tasks: {}", stats[0]);
        logger.info("Completed Tasks: {}", stats[1]);
        logger.info("Completion Rate: {:.2f}%", (stats[1] * 100.0 / Math.max(stats[0], 1)));
        logger.info("Total Waste Collected: {:.2f} kg", totalWaste);
    }

    /**
     * List all registered workers
     */
    public void listAllWorkers() {
        List<Worker> workers = workerRepository.findAllActive();
        logger.info("=== Registered Workers ===");
        for (Worker worker : workers) {
            logger.info("ID: {} | Name: {} | Role: {} | Email: {}", 
                       worker.getId(), worker.getFullName(), worker.getRole(), worker.getEmail());
        }
    }

    /**
     * Get workers by role
     */
    public void listWorkersByRole(WorkerRole role) {
        List<Worker> workers = workerRepository.findByRole(role.toString());
        logger.info("=== {} ===", role.getDisplayName() + "s");
        for (Worker worker : workers) {
            logger.info("Name: {} | Email: {}", worker.getFullName(), worker.getEmail());
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Waste Management Attendance System...");
        
        try {
            WasteManagementApplication app = new WasteManagementApplication();
            
            // Example: Register workers
            app.registerWorker("EMP001", "John Cleaner", "john@waste.com", "9999000001", 
                             WorkerRole.CLEANER, "data/faces/john_face.jpg");
            app.registerWorker("EMP002", "Mike Driver", "mike@waste.com", "9999000002", 
                             WorkerRole.DRIVER, "data/faces/mike_face.jpg");
            app.registerWorker("EMP003", "Sarah Helper", "sarah@waste.com", "9999000003", 
                             WorkerRole.HELPER, "data/faces/sarah_face.jpg");
            
            // List all workers
            app.listAllWorkers();
            
            // Display statistics
            app.displayTodayStatistics();
            app.displayWasteStatistics();
            
            logger.info("Application running successfully!");
            
        } catch (Exception e) {
            logger.error("Application error", e);
        } finally {
            HibernateConfig.shutdown();
        }
    }
}
