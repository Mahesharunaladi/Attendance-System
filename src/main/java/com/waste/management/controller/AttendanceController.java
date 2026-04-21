package com.waste.management.controller;

import com.waste.management.entity.Worker;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.service.AttendanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Attendance API endpoints
 */
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
    public Map<String, Object> processCheckIn(String employeeId, String imagePath, 
                                               Double latitude, Double longitude) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
            
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found: " + employeeId);
                response.put("code", 404);
                return response;
            }

            Optional<?> result = attendanceService.recordCheckIn(worker.get(), imagePath, latitude, longitude);
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Check-in successful");
                response.put("code", 200);
                response.put("data", result.get());
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
    public Map<String, Object> processCheckOut(String employeeId, String imagePath,
                                                Double latitude, Double longitude) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
            
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found: " + employeeId);
                response.put("code", 404);
                return response;
            }

            Optional<?> result = attendanceService.recordCheckOut(worker.get(), imagePath, latitude, longitude);
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Check-out successful");
                response.put("code", 200);
                response.put("data", result.get());
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
     * GET /api/attendance/status
     * Get today's attendance statistics
     */
    public Map<String, Object> getTodayAttendanceStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
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
    public Map<String, Object> getAttendanceReport(String startDate, String endDate) {
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
    public Map<String, Object> getWorkerAttendanceReport(Long workerId, String startDate, String endDate) {
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
}
