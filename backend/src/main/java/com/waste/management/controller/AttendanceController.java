package com.waste.management.controller;

import com.waste.management.entity.AttendanceRecord;
import com.waste.management.entity.Worker;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Attendance API endpoints
 */
@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttendanceController {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    
    private final AttendanceService attendanceService;
    private final WorkerRepository workerRepository;

    public AttendanceController(AttendanceService attendanceService,
                              WorkerRepository workerRepository) {
        this.attendanceService = attendanceService;
        this.workerRepository = workerRepository;
    }

    /**
     * POST /api/attendance/checkin
     * Process check-in for a worker
     */
    @PostMapping("/checkin")
    public Map<String, Object> processCheckIn(
            @RequestParam String employeeId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
            
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found: " + employeeId);
                response.put("code", 404);
                return response;
            }

            String imagePath = null;
            MultipartFile uploadedImage = resolveUploadedImage(image, imageFile);
            if (uploadedImage != null && !uploadedImage.isEmpty()) {
                imagePath = saveUploadedFile(uploadedImage);
            }

            Optional<?> result = attendanceService.recordCheckIn(worker.get(), imagePath, latitude, longitude);
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Check-in successful");
                response.put("code", 200);
                response.put("data", buildAttendanceRecordResponse((AttendanceRecord) result.get()));
            } else {
                response.put("success", false);
                response.put("message", "Face verification failed");
                response.put("code", 401);
            }
        } catch (Exception e) {
            logger.error("Error processing check-in", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * POST /api/attendance/checkout
     * Process check-out for a worker
     */
    @PostMapping("/checkout")
    public Map<String, Object> processCheckOut(@RequestParam String employeeId,
                                               @RequestParam(required = false) Double latitude,
                                               @RequestParam(required = false) Double longitude,
                                               @RequestParam(name = "image", required = false) MultipartFile image,
                                               @RequestParam(name = "imageFile", required = false) MultipartFile imageFile) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
            
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found: " + employeeId);
                response.put("code", 404);
                return response;
            }

            String imagePath = null;
            MultipartFile uploadedImage = resolveUploadedImage(image, imageFile);
            if (uploadedImage != null && !uploadedImage.isEmpty()) {
                imagePath = saveUploadedFile(uploadedImage);
            }

            Optional<?> result = attendanceService.recordCheckOut(worker.get(), imagePath, latitude, longitude);
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Check-out successful");
                response.put("code", 200);
                response.put("data", buildAttendanceRecordResponse((AttendanceRecord) result.get()));
            } else {
                response.put("success", false);
                response.put("message", "No active check-in found");
                response.put("code", 404);
            }
        } catch (Exception e) {
            logger.error("Error processing check-out", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/attendance/today
     * Get today's attendance statistics or specific worker's status
     */
    @GetMapping("/today")
    public Map<String, Object> getTodayAttendanceStatus(
            @RequestParam(required = false) String employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (employeeId != null && !employeeId.isEmpty()) {
                // Get status for specific worker
                Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
                if (worker.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Worker not found");
                    response.put("code", 404);
                    return response;
                }
                
                Optional<AttendanceRecord> todayAttendance = attendanceService.getTodayAttendanceForWorker(worker.get());
                
                Map<String, Object> status = new HashMap<>();
                if (todayAttendance.isPresent()) {
                    AttendanceRecord record = todayAttendance.get();
                    status.put("checkedIn", true);
                    status.put("checkInTime", record.getCheckInTime());
                    
                    boolean hasCheckOut = record.getCheckOutTime() != null;
                    status.put("checkedOut", hasCheckOut);
                    if (hasCheckOut) {
                        status.put("checkOutTime", record.getCheckOutTime());
                    }
                } else {
                    status.put("checkedIn", false);
                    status.put("checkedOut", false);
                }
                
                response.put("success", true);
                response.put("data", status);
                response.put("code", 200);
            } else {
                // Get overall statistics
                List<Worker> allWorkers = workerRepository.findAllActive();
                long presentCount = attendanceService.getTodayAttendanceCount();
                long absentCount = attendanceService.getAbsenteeCount(allWorkers, LocalDate.now());
                
                Map<String, Object> stats = new HashMap<>();
                stats.put("total_workers", allWorkers.size());
                stats.put("present", presentCount);
                stats.put("absent", absentCount);
                stats.put("attendance_rate", String.format("%.2f%%", 
                        (presentCount * 100.0 / Math.max(allWorkers.size(), 1))));
                
                response.put("success", true);
                response.put("data", stats);
                response.put("code", 200);
            }
        } catch (Exception e) {
            logger.error("Error getting attendance status", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/attendance/report
     * Get attendance report for date range
     */
    @GetMapping("/report")
    public Map<String, Object> getAttendanceReport(@RequestParam String startDate,
                                                   @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            var records = attendanceService.getAttendanceReport(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("total_records", records.size());
            data.put("from_date", startDate);
            data.put("to_date", endDate);
            data.put("records", records);
            
            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting attendance report", e);
            response.put("success", false);
            response.put("message", "Invalid date format or server error: " + e.getMessage());
            response.put("code", 400);
        }
        
        return response;
    }

    /**
     * GET /api/attendance/worker/{workerId}
     * Get attendance report for specific worker
     */
    @GetMapping("/report/{workerId}")
    public Map<String, Object> getWorkerAttendanceReport(@PathVariable Long workerId,
                                                         @RequestParam String startDate,
                                                         @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findById(workerId);
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
                return response;
            }
            
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            var records = attendanceService.getWorkerAttendanceReport(workerId, start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("worker_id", workerId);
            data.put("worker_name", worker.get().getFullName());
            data.put("total_records", records.size());
            data.put("from_date", startDate);
            data.put("to_date", endDate);
            data.put("records", records);
            
            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting worker attendance report", e);
            response.put("success", false);
            response.put("message", "Invalid request: " + e.getMessage());
            response.put("code", 400);
        }
        
        return response;
    }

    /**
     * POST /api/attendance/identify-face
     * Identify worker from captured face image
     */
    @PostMapping("/identify-face")
    public Map<String, Object> identifyWorkerFromFace(
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "imageFile", required = false) MultipartFile imageFile) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            MultipartFile uploadedImage = resolveUploadedImage(image, imageFile);
            if (uploadedImage == null || uploadedImage.isEmpty()) {
                response.put("success", false);
                response.put("message", "Image file is required");
                response.put("code", 400);
                return response;
            }

            String imagePath = saveUploadedFile(uploadedImage);

            // Call face recognition service to identify worker
            Optional<Worker> identifiedWorker = attendanceService.identifyWorkerFromFace(imagePath);
            
            if (identifiedWorker.isPresent()) {
                Worker worker = identifiedWorker.get();
                
                Map<String, Object> workerData = new HashMap<>();
                workerData.put("id", worker.getId());
                workerData.put("fullName", worker.getFullName());
                workerData.put("employeeId", worker.getEmployeeId());
                workerData.put("aadharNumber", worker.getAadharNumber());
                workerData.put("phoneNumber", worker.getPhoneNumber());
                workerData.put("gender", worker.getGender());
                workerData.put("caste", worker.getCaste());
                workerData.put("role", worker.getRole());
                workerData.put("department", worker.getDepartment());
                workerData.put("email", worker.getEmail());
                
                response.put("success", true);
                response.put("message", "Worker identified successfully");
                response.put("worker", workerData);
                response.put("code", 200);
            } else {
                response.put("success", false);
                response.put("message", "No matching worker found for the provided face");
                response.put("code", 404);
            }
        } catch (Exception e) {
            logger.error("Error identifying worker from face", e);
            response.put("success", false);
            response.put("message", "Error identifying worker: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    private MultipartFile resolveUploadedImage(MultipartFile image, MultipartFile imageFile) {
        if (image != null && !image.isEmpty()) {
            return image;
        }
        return imageFile;
    }

    private Map<String, Object> buildAttendanceRecordResponse(AttendanceRecord record) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", record.getId());
        data.put("checkInTime", record.getCheckInTime());
        data.put("checkOutTime", record.getCheckOutTime());
        data.put("status", record.getStatus() != null ? record.getStatus().name() : null);
        data.put("faceMatchConfidence", record.getFaceMatchConfidence());
        data.put("locationLatitude", record.getLocationLatitude());
        data.put("locationLongitude", record.getLocationLongitude());
        data.put("imageCapturedPath", record.getImageCapturedPath());
        return data;
    }

    /**
     * Save uploaded file to uploads directory
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads";
        Files.createDirectories(Paths.get(uploadDir));
        
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + "/" + fileName;
        
        Files.write(Paths.get(filePath), file.getBytes());
        logger.info("File saved: {}", filePath);
        
        return filePath;
    }
}
