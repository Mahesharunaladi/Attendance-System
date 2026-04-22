package com.waste.management.controller;

import com.waste.management.entity.Worker;
import com.waste.management.entity.WorkerRole;
import com.waste.management.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Worker Management API endpoints
 */
@RestController
@RequestMapping("/api/workers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WorkerController {
    private static final Logger logger = LoggerFactory.getLogger(WorkerController.class);
    
    private final WorkerRepository workerRepository;

    public WorkerController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    /**
     * GET /api/workers
     * Get all workers
     */
    @GetMapping
    public List<Worker> getAllWorkers() {
        logger.info("Fetching all workers");
        return workerRepository.findAll();
    }

    /**
     * GET /api/workers/{id}
     * Get worker by ID
     */
    @GetMapping("/{id}")
    public Map<String, Object> getWorkerById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Worker> worker = workerRepository.findById(id);
            if (worker.isPresent()) {
                response.put("success", true);
                response.put("data", worker.get());
            } else {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("code", 500);
        }
        return response;
    }

    /**
     * GET /api/workers/employee/{employeeId}
     * Get worker by employee ID
     */
    @GetMapping("/employee/{employeeId}")
    public Map<String, Object> getWorkerByEmployeeId(@PathVariable String employeeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
            if (worker.isPresent()) {
                response.put("success", true);
                response.put("data", worker.get());
            } else {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("code", 500);
        }
        return response;
    }

    /**
     * POST /api/workers/register
     * Register a new worker
     */
    @PostMapping("/register")
    public Map<String, Object> registerWorker(
            @RequestParam(required = false) String employeeId,
            @RequestParam String fullName,
            @RequestParam String phoneNumber,
            @RequestParam String aadharNumber,
            @RequestParam(required = false) String role,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!isValidPhoneNumber(phoneNumber)) {
                response.put("success", false);
                response.put("message", "Invalid phone number format");
                response.put("code", 400);
                return response;
            }

            if (!isValidAadharNumber(aadharNumber)) {
                response.put("success", false);
                response.put("message", "Invalid Aadhar number format");
                response.put("code", 400);
                return response;
            }

            MultipartFile uploadedImage = resolveUploadedImage(image, imageFile);
            if (uploadedImage == null || uploadedImage.isEmpty()) {
                response.put("success", false);
                response.put("message", "Live image is required for registration");
                response.put("code", 400);
                return response;
            }

            String resolvedEmployeeId = (employeeId == null || employeeId.isBlank())
                    ? generateEmployeeId()
                    : employeeId.trim().toUpperCase(Locale.ROOT);

            Optional<Worker> existing = workerRepository.findByEmployeeId(resolvedEmployeeId);
            if (existing.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee ID already exists");
                response.put("code", 409);
                return response;
            }

            if (workerRepository.findByAadharNumber(aadharNumber).isPresent()) {
                response.put("success", false);
                response.put("message", "Aadhar number already exists");
                response.put("code", 409);
                return response;
            }

            WorkerRole resolvedRole = WorkerRole.CLEANER;
            if (role != null && !role.isBlank()) {
                resolvedRole = WorkerRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
            }

            String generatedEmail = buildEmail(fullName, resolvedEmployeeId);
            String facialDataPath = saveUploadedFile(uploadedImage, resolvedEmployeeId);

            Worker worker = new Worker();
            worker.setEmployeeId(resolvedEmployeeId);
            worker.setFullName(fullName);
            worker.setEmail(generatedEmail);
            worker.setPhoneNumber(phoneNumber);
            worker.setAadharNumber(aadharNumber);
            worker.setRole(resolvedRole);
            worker.setFacialDataPath(facialDataPath);
            worker.setDepartment("Waste Management");
            worker.setActive(true);
            worker.setCreatedAt(LocalDateTime.now());

            Worker savedWorker = workerRepository.save(worker);

            Map<String, Object> data = new HashMap<>();
            data.put("worker_id", savedWorker.getId());
            data.put("employee_id", savedWorker.getEmployeeId());
            data.put("name", savedWorker.getFullName());
            data.put("phone_number", savedWorker.getPhoneNumber());
            data.put("aadhar_number", savedWorker.getAadharNumber());
            data.put("role", savedWorker.getRole().getDisplayName());
            data.put("email", savedWorker.getEmail());
            data.put("facial_data_path", savedWorker.getFacialDataPath());

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
     * GET /api/workers/role/{role}
     * Get workers by role
     */
    @GetMapping("/role/{role}")
    public Map<String, Object> getWorkersByRole(@PathVariable String role) {
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
    @PutMapping("/{workerId}")
    public Map<String, Object> updateWorker(@PathVariable Long workerId,
                                           @RequestParam(required = false) String email,
                                           @RequestParam(required = false) String phoneNumber) {
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

    private boolean isValidAadharNumber(String aadharNumber) {
        return aadharNumber != null && aadharNumber.matches("^[0-9]{12}$");
    }

    private MultipartFile resolveUploadedImage(MultipartFile image, MultipartFile imageFile) {
        if (image != null && !image.isEmpty()) {
            return image;
        }
        return imageFile;
    }

    private String generateEmployeeId() {
        return "EMP" + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    private String buildEmail(String fullName, String employeeId) {
        String baseName = Normalizer.normalize(fullName == null ? "" : fullName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".");
        baseName = baseName.replaceAll("(^\\.+|\\.+$)", "");
        if (baseName.isBlank()) {
            baseName = employeeId.toLowerCase(Locale.ROOT);
        }
        return baseName + "@waste.com";
    }

    private String saveUploadedFile(MultipartFile file, String employeeId) throws IOException {
        Path uploadDir = Paths.get("uploads", "workers");
        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename() == null ? "capture.jpg" : file.getOriginalFilename();
        String sanitizedName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = employeeId + "_" + System.currentTimeMillis() + "_" + sanitizedName;
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, file.getBytes());
        logger.info("Saved worker registration image: {}", filePath);
        return filePath.toString();
    }
}
