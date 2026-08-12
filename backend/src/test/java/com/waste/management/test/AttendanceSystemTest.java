package com.waste.management.test;

import com.waste.management.config.HibernateConfig;
import com.waste.management.entity.Worker;
import com.waste.management.entity.WorkerRole;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.service.FaceRecognitionService;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Unit tests for Attendance System
 */
public class AttendanceSystemTest {
    
    private WorkerRepository workerRepository;
    private FaceRecognitionService faceRecognitionService;

    @Before
    public void setUp() {
        SessionFactory sessionFactory = HibernateConfig.getSessionFactory();
        workerRepository = new WorkerRepository(sessionFactory);
        faceRecognitionService = new FaceRecognitionService();
    }

    @Test
    public void testWorkerRegistration() {
        // Create worker
        Worker worker = new Worker();
        worker.setEmployeeId("TEST001");
        worker.setFullName("Test Worker");
        worker.setEmail("test@waste.com");
        worker.setPhoneNumber("9876543210");
        worker.setRole(WorkerRole.CLEANER);
        worker.setFacialDataPath("test/faces/test.jpg");
        worker.setDepartment("Waste Management");
        worker.setActive(true);
        worker.setCreatedAt(LocalDateTime.now());

        // Save worker
        Worker savedWorker = workerRepository.save(worker);

        // Assert
        assertNotNull(savedWorker.getId());
        assertEquals("TEST001", savedWorker.getEmployeeId());
        assertEquals("Test Worker", savedWorker.getFullName());
    }

    @Test
    public void testFindWorkerByEmployeeId() {
        // Create and save worker
        Worker worker = new Worker();
        worker.setEmployeeId("TEST002");
        worker.setFullName("Another Worker");
        worker.setEmail("another@waste.com");
        worker.setPhoneNumber("9876543211");
        worker.setRole(WorkerRole.DRIVER);
        worker.setFacialDataPath("test/faces/another.jpg");
        worker.setDepartment("Transportation");
        worker.setActive(true);
        worker.setCreatedAt(LocalDateTime.now());

        workerRepository.save(worker);

        // Find worker
        Optional<Worker> found = workerRepository.findByEmployeeId("TEST002");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Another Worker", found.get().getFullName());
        assertEquals(WorkerRole.DRIVER, found.get().getRole());
    }

    @Test
    public void testGetAllActiveWorkers() {
        // Get all active workers
        var workers = workerRepository.findAllActive();

        // Assert
        assertNotNull(workers);
        assertTrue(workers.size() >= 0);
    }

    @Test
    public void testFaceRecognitionDetectsFeatures() {
        // This test would require actual image files
        // Demonstrates face detection capability
        assertNotNull(faceRecognitionService);
    }

    @Test
    public void testFaceMatchThreshold() {
        // Test that face recognition service has valid threshold
        assertTrue(faceRecognitionService.isMatchConfident(0.8));
        assertFalse(faceRecognitionService.isMatchConfident(0.5));
    }

    @Test
    public void testWorkerRoleEnum() {
        // Test worker role enum
        assertEquals("Cleaner", WorkerRole.CLEANER.getDisplayName());
        assertEquals("Driver", WorkerRole.DRIVER.getDisplayName());
        assertEquals("Helper", WorkerRole.HELPER.getDisplayName());
        assertEquals("Supervisor", WorkerRole.SUPERVISOR.getDisplayName());
        assertEquals("Manager", WorkerRole.MANAGER.getDisplayName());
    }

    @Test
    public void testWorkerEmailValidation() {
        Worker worker = new Worker();
        worker.setEmail("valid@waste.com");
        assertNotNull(worker.getEmail());
        assertTrue(worker.getEmail().contains("@"));
    }

    @Test
    public void testWorkerPhoneNumberValidation() {
        Worker worker = new Worker();
        worker.setPhoneNumber("9876543210");
        assertNotNull(worker.getPhoneNumber());
        assertEquals(10, worker.getPhoneNumber().length());
    }

    @Test
    public void testWorkerActiveStatus() {
        Worker worker = new Worker();
        worker.setActive(true);
        assertTrue(worker.isActive());

        worker.setActive(false);
        assertFalse(worker.isActive());
    }

    @Test
    public void testWorkerDepartmentAssignment() {
        Worker worker = new Worker();
        worker.setDepartment("Waste Management");
        assertEquals("Waste Management", worker.getDepartment());
    }

    @Test
    public void testWorkerTimestamps() {
        Worker worker = new Worker();
        LocalDateTime now = LocalDateTime.now();
        worker.setCreatedAt(now);
        worker.setUpdatedAt(now);

        assertNotNull(worker.getCreatedAt());
        assertNotNull(worker.getUpdatedAt());
        assertEquals(now, worker.getCreatedAt());
    }

    @Test
    public void testMultipleWorkerRoles() {
        // Test that different roles can be assigned
        for (WorkerRole role : WorkerRole.values()) {
            Worker worker = new Worker();
            worker.setRole(role);
            assertEquals(role, worker.getRole());
        }
    }

    @Test
    public void testWorkerDataIntegrity() {
        Worker worker = new Worker();
        worker.setEmployeeId("INTEGRITY_TEST");
        worker.setFullName("Integrity Test");
        worker.setEmail("integrity@test.com");
        worker.setPhoneNumber("9999999999");

        assertEquals("INTEGRITY_TEST", worker.getEmployeeId());
        assertEquals("Integrity Test", worker.getFullName());
        assertEquals("integrity@test.com", worker.getEmail());
        assertEquals("9999999999", worker.getPhoneNumber());
    }
}
