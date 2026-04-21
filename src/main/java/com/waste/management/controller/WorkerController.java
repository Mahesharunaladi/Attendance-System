package com.waste.management.controller;

import com.waste.management.entity.Worker;
import com.waste.management.entity.WorkerRole;
import com.waste.management.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Worker Management API endpoints
 */
public class WorkerController {
    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);
    
    private final WorkerRepository workerRepository;

    public WorkerController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    /**
     * POST /api/workers/register
     * Register a new worker
     */
    public Map<String, Object> registerWorker(String employeeId, String fullName, String email,
                                             String phoneNumber, String role, String facialDataPath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate email
            if (!isValidEmail(email)) {
                response.put("success", false);
                response.put("message", "Invalid email format");
                response.put("code", 400);
                return response;
            }

            // Validate phone number
            if (!isValidPhoneNumber(phoneNumber)) {
                response.put("success", false);
                response.put("message", "Invalid phone number format");
                response.put("code", 400);
                return response;
            }

            // Check if employee already exists
            Optional<Worker> existing = workerRepository.findByEmployeeId(employeeId);
            if (existing.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee ID already exists");
                response.put("code", 409);
                return response;
            }

            Worker worker = new Worker();
            worker.setEmployeeId(employeeId);
            worker.setFullName(fullName);
            worker.setEmail(email);
            worker.setPhoneNumber(phoneNumber);
            worker.setRole(WorkerRole.valueOf(role.toUpperCase()));
            worker.setFacialDataPath(facialDataPath);
            worker.setDepartment("Waste Management");
            worker.setActive(true);
            worker.setCreatedAt(LocalDateTime.now());

            Worker savedWorker = workerRepository.save(worker);

            Map<String, Object> data = new HashMap<>();
            data.put("worker_id", savedWorker.getId());
            data.put("employee_id", savedWorker.getEmployeeId());
            data.put("name", savedWorker.getFullName());
            data.put("role", savedWorker.getRole().getDisplayName());

            response.put("success", true);
            response.put("message", "Worker registered successfully");
            response.put("data", data);
            response.put("code", 201);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid role: " + e.getMessage());
            response.put("code", 400);
        } catch (Exception e) {
            logger.error("Error registering worker", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/workers/{workerId}
     * Get worker details
     */
    public Map<String, Object> getWorker(Long workerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findById(workerId);
            
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
                return response;
            }

            Worker w = worker.get();
            Map<String, Object> data = new HashMap<>();
            data.put("worker_id", w.getId());
            data.put("employee_id", w.getEmployeeId());
            data.put("full_name", w.getFullName());
            data.put("email", w.getEmail());
            data.put("phone_number", w.getPhoneNumber());
            data.put("role", w.getRole().getDisplayName());
            data.put("department", w.getDepartment());
            data.put("active", w.isActive());

            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting worker", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/workers/all
     * Get all active workers
     */
    public Map<String, Object> getAllWorkers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Worker> workers = workerRepository.findAllActive();
            
            Map<String, Object> data = new HashMap<>();
            data.put("total_workers", workers.size());
            data.put("workers", workers);

            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting all workers", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/workers/role/{role}
     * Get workers by role
     */
    public Map<String, Object> getWorkersByRole(String role) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate role
            try {
                WorkerRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("message", "Invalid role: " + role);
                response.put("code", 400);
                return response;
            }

            List<Worker> workers = workerRepository.findByRole(role.toUpperCase());
            
            Map<String, Object> data = new HashMap<>();
            data.put("role", role);
            data.put("total_workers", workers.size());
            data.put("workers", workers);

            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting workers by role", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * PUT /api/workers/{workerId}
     * Update worker information
     */
    public Map<String, Object> updateWorker(Long workerId, String email, String phoneNumber) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findById(workerId);
            
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
                return response;
            }

            Worker w = worker.get();
            
            if (email != null && !email.isEmpty()) {
                if (!isValidEmail(email)) {
                    response.put("success", false);
                    response.put("message", "Invalid email format");
                    response.put("code", 400);
                    return response;
                }
                w.setEmail(email);
            }

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                if (!isValidPhoneNumber(phoneNumber)) {
                    response.put("success", false);
                    response.put("message", "Invalid phone number format");
                    response.put("code", 400);
                    return response;
                }
                w.setPhoneNumber(phoneNumber);
            }

            w.setUpdatedAt(LocalDateTime.now());
            workerRepository.save(w);

            response.put("success", true);
            response.put("message", "Worker updated successfully");
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error updating worker", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * DELETE /api/workers/{workerId}
     * Deactivate worker
     */
    public Map<String, Object> deactivateWorker(Long workerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findById(workerId);
            
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
                return response;
            }

            Worker w = worker.get();
            w.setActive(false);
            w.setUpdatedAt(LocalDateTime.now());
            workerRepository.save(w);

            response.put("success", true);
            response.put("message", "Worker deactivated successfully");
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error deactivating worker", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    /**
     * Validate phone number format
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[0-9]{10}$");
    }
}
