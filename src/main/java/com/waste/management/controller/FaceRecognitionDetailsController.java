package com.waste.management.controller;

import com.waste.management.dto.ApiResponse;
import com.waste.management.dto.WorkerDetailsDto;
import com.waste.management.entity.WorkerRole;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.service.FaceRecognitionWithDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST API Controller for Face Recognition with automatic worker details fetching
 * 
 * Endpoints:
 * - POST /api/recognition/driver/details - Recognize driver and fetch details
 * - POST /api/recognition/worker/details - Recognize worker by role and fetch details
 * - POST /api/recognition/auto/details - Auto-detect role and fetch details
 * - GET /api/recognition/worker/{id} - Get worker details by ID
 * - GET /api/recognition/driver/all - Get all driver details
 */
@RestController
@RequestMapping("/api/recognition")
public class FaceRecognitionDetailsController {

    @Autowired
    private WorkerRepository workerRepository;

    private FaceRecognitionWithDetailsService detailsFetchingService;

    // Directory to save uploaded images
    private static final String UPLOAD_DIR = "uploads/faces";

    /**
     * Get face recognition service (lazy initialization)
     */
    private FaceRecognitionWithDetailsService getFaceRecognitionService() {
        if (detailsFetchingService == null) {
            detailsFetchingService = new FaceRecognitionWithDetailsService(workerRepository);
            new File(UPLOAD_DIR).mkdirs();
        }
        return detailsFetchingService;
    }

    /**
     * Recognize driver from captured image and fetch complete details
     * 
     * POST /api/recognition/driver/details
     * Content-Type: multipart/form-data
     * 
     * @param file Captured image file
     * @return WorkerDetailsDto with driver information or error message
     */
    @PostMapping("/driver/details")
    public ResponseEntity<ApiResponse<WorkerDetailsDto>> recognizeDriverAndFetchDetails(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Image file is empty", null)
                );
            }

            // Save uploaded file
            String imagePath = saveUploadedFile(file);

            // Recognize driver and fetch details
            Optional<WorkerDetailsDto> driverDetails = getFaceRecognitionService().recognizeDriverAndFetchDetails(imagePath);

            if (driverDetails.isPresent()) {
                return ResponseEntity.ok().body(
                    new ApiResponse<>(true, "Driver recognized successfully", driverDetails.get())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "Driver not recognized. Face does not match any registered driver.", null)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Error recognizing driver: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Recognize worker by specific role and fetch details
     * 
     * POST /api/recognition/worker/details?role=DRIVER
     * Content-Type: multipart/form-data
     * 
     * @param file Captured image file
     * @param role Worker role (DRIVER, CLEANER, HELPER, SUPERVISOR, MANAGER)
     * @return WorkerDetailsDto with worker information or error message
     */
    @PostMapping("/worker/details")
    public ResponseEntity<ApiResponse<WorkerDetailsDto>> recognizeWorkerByRoleAndFetchDetails(
            @RequestParam("image") MultipartFile file,
            @RequestParam("role") String role) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Image file is empty", null)
                );
            }

            // Parse role
            WorkerRole workerRole;
            try {
                workerRole = WorkerRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Invalid role: " + role + ". Valid roles: DRIVER, CLEANER, HELPER, SUPERVISOR, MANAGER", null)
                );
            }

            // Save uploaded file
            String imagePath = saveUploadedFile(file);

            // Recognize worker and fetch details
            Optional<WorkerDetailsDto> workerDetails = getFaceRecognitionService().recognizeWorkerAndFetchDetails(imagePath, workerRole);

            if (workerDetails.isPresent()) {
                return ResponseEntity.ok().body(
                    new ApiResponse<>(true, "Worker recognized successfully", workerDetails.get())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "No matching worker found for role: " + role, null)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Error recognizing worker: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Auto-detect worker role and fetch details
     * 
     * POST /api/recognition/auto/details
     * Content-Type: multipart/form-data
     * 
     * Tries to recognize the face against all registered workers across all roles
     * 
     * @param file Captured image file
     * @return WorkerDetailsDto with worker information or error message
     */
    @PostMapping("/auto/details")
    public ResponseEntity<ApiResponse<WorkerDetailsDto>> recognizeAnyWorkerAndFetchDetails(
            @RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Image file is empty", null)
                );
            }

            // Save uploaded file
            String imagePath = saveUploadedFile(file);

            // Auto-detect role and fetch details
            Optional<WorkerDetailsDto> workerDetails = getFaceRecognitionService().recognizeAnyWorkerAndFetchDetails(imagePath);

            if (workerDetails.isPresent()) {
                return ResponseEntity.ok().body(
                    new ApiResponse<>(true, "Worker recognized successfully", workerDetails.get())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "Could not recognize worker face against any registered workers", null)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Error recognizing worker: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Fetch worker details by ID (without face recognition)
     * 
     * GET /api/recognition/worker/{id}
     * 
     * @param workerId Worker ID
     * @return WorkerDetailsDto or error message
     */
    @GetMapping("/worker/{id}")
    public ResponseEntity<ApiResponse<WorkerDetailsDto>> getWorkerDetailsById(@PathVariable Long workerId) {
        try {
            Optional<WorkerDetailsDto> workerDetails = getFaceRecognitionService().getWorkerDetailsById(workerId);

            if (workerDetails.isPresent()) {
                return ResponseEntity.ok().body(
                    new ApiResponse<>(true, "Worker details retrieved successfully", workerDetails.get())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "No worker found with ID: " + workerId, null)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Error fetching worker details: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Fetch worker details by employee ID (without face recognition)
     * 
     * GET /api/recognition/worker/employee/{employeeId}
     * 
     * @param employeeId Employee ID
     * @return WorkerDetailsDto or error message
     */
    @GetMapping("/worker/employee/{employeeId}")
    public ResponseEntity<ApiResponse<WorkerDetailsDto>> getWorkerDetailsByEmployeeId(@PathVariable String employeeId) {
        try {
            Optional<WorkerDetailsDto> workerDetails = getFaceRecognitionService().getWorkerDetailsByEmployeeId(employeeId);

            if (workerDetails.isPresent()) {
                return ResponseEntity.ok().body(
                    new ApiResponse<>(true, "Worker details retrieved successfully", workerDetails.get())
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "No worker found with employee ID: " + employeeId, null)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Error fetching worker details: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Get all driver details
     * 
     * GET /api/recognition/driver/all
     * 
     * @return List of all driver details
     */
    @GetMapping("/driver/all")
    public ResponseEntity<ApiResponse<List<WorkerDetailsDto>>> getAllDriverDetails() {
        try {
            List<WorkerDetailsDto> allDrivers = getFaceRecognitionService().getAllDriverDetails();

            if (!allDrivers.isEmpty()) {
                return ResponseEntity.ok().body(
                    new ApiResponse<>(true, "Retrieved " + allDrivers.size() + " driver(s)", allDrivers)
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(false, "No registered drivers found in the system", null)
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Error fetching drivers: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Save uploaded file to disk
     * 
     * @param file MultipartFile to save
     * @return Path to saved file
     */
    private String saveUploadedFile(MultipartFile file) throws Exception {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        
        // Create directories if they don't exist
        Files.createDirectories(filePath.getParent());
        
        // Save file
        Files.write(filePath, file.getBytes());
        
        return filePath.toAbsolutePath().toString();
    }

    /**
     * Health check endpoint
     * 
     * GET /api/recognition/health
     * 
     * @return Status message
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok().body(
            new ApiResponse<>(true, "Face Recognition Service is running", "OK")
        );
    }
}
