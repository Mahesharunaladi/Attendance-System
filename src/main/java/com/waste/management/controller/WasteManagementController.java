package com.waste.management.controller;

import com.waste.management.entity.Worker;
import com.waste.management.entity.WasteTask;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.service.WasteManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Waste Management API endpoints
 */
public class WasteManagementController {
    private static final Logger logger = LoggerFactory.getLogger(WasteManagementController.class);
    
    private final WasteManagementService wasteManagementService;
    private final WorkerRepository workerRepository;

    public WasteManagementController(WasteManagementService wasteManagementService,
                                   WorkerRepository workerRepository) {
        this.wasteManagementService = wasteManagementService;
        this.workerRepository = workerRepository;
    }

    /**
     * POST /api/waste-tasks/create
     * Create a new waste collection task
     */
    public Map<String, Object> createTask(String area, String wasteType, Double estimatedWeight) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            WasteTask task = wasteManagementService.createWasteTask(area, wasteType, estimatedWeight);
            
            Map<String, Object> data = new HashMap<>();
            data.put("task_id", task.getId());
            data.put("task_name", task.getTaskId());
            data.put("area", task.getArea());
            data.put("waste_type", task.getWasteType());
            data.put("estimated_weight", task.getEstimatedWeight());
            data.put("status", task.getStatus().getDisplayName());
            
            response.put("success", true);
            response.put("message", "Task created successfully");
            response.put("data", data);
            response.put("code", 201);
        } catch (Exception e) {
            logger.error("Error creating waste task", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * POST /api/waste-tasks/{taskId}/assign-worker
     * Assign worker to a waste task
     */
    public Map<String, Object> assignWorkerToTask(Long taskId, Long workerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findById(workerId);
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
                return response;
            }

            Optional<WasteTask> result = wasteManagementService.assignWorkerToTask(taskId, worker.get());
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Worker assigned to task");
                response.put("code", 200);
            } else {
                response.put("success", false);
                response.put("message", "Task not found");
                response.put("code", 404);
            }
        } catch (Exception e) {
            logger.error("Error assigning worker to task", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * POST /api/waste-tasks/{taskId}/assign-driver
     * Assign driver to a waste task
     */
    public Map<String, Object> assignDriverToTask(Long taskId, Long driverId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> driver = workerRepository.findById(driverId);
            if (driver.isEmpty()) {
                response.put("success", false);
                response.put("message", "Driver not found");
                response.put("code", 404);
                return response;
            }

            Optional<WasteTask> result = wasteManagementService.assignDriverToTask(taskId, driver.get());
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Driver assigned to task");
                response.put("code", 200);
            } else {
                response.put("success", false);
                response.put("message", "Task not found");
                response.put("code", 404);
            }
        } catch (Exception e) {
            logger.error("Error assigning driver to task", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * POST /api/waste-tasks/{taskId}/complete
     * Mark task as completed with weight and location
     */
    public Map<String, Object> completeTask(Long taskId, Double actualWeight, 
                                            Double latitude, Double longitude) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<WasteTask> result = wasteManagementService.completeTask(
                taskId, actualWeight, latitude, longitude
            );
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Task completed successfully");
                response.put("code", 200);
            } else {
                response.put("success", false);
                response.put("message", "Task not found");
                response.put("code", 404);
            }
        } catch (Exception e) {
            logger.error("Error completing task", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/waste-tasks/pending
     * Get all pending tasks
     */
    public Map<String, Object> getPendingTasks() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<WasteTask> tasks = wasteManagementService.getPendingTasks();
            
            Map<String, Object> data = new HashMap<>();
            data.put("total_tasks", tasks.size());
            data.put("tasks", tasks);
            
            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting pending tasks", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/waste-tasks/in-progress
     * Get all in-progress tasks
     */
    public Map<String, Object> getInProgressTasks() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<WasteTask> tasks = wasteManagementService.getInProgressTasks();
            
            Map<String, Object> data = new HashMap<>();
            data.put("total_tasks", tasks.size());
            data.put("tasks", tasks);
            
            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting in-progress tasks", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/waste-tasks/statistics
     * Get waste collection statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long[] stats = wasteManagementService.getCompletionStatistics();
            Double totalWaste = wasteManagementService.getTotalWasteCollected();
            
            Map<String, Object> data = new HashMap<>();
            data.put("total_tasks", stats[0]);
            data.put("completed_tasks", stats[1]);
            data.put("completion_rate", String.format("%.2f%%", 
                    (stats[1] * 100.0 / Math.max(stats[0], 1))));
            data.put("total_waste_collected_kg", totalWaste);
            
            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting waste statistics", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * GET /api/waste-tasks/worker/{workerId}
     * Get tasks assigned to a specific worker
     */
    public Map<String, Object> getWorkerTasks(Long workerId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Worker> worker = workerRepository.findById(workerId);
            if (worker.isEmpty()) {
                response.put("success", false);
                response.put("message", "Worker not found");
                response.put("code", 404);
                return response;
            }

            List<WasteTask> tasks = wasteManagementService.getTasksAssignedToWorker(workerId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("worker_id", workerId);
            data.put("worker_name", worker.get().getFullName());
            data.put("total_assigned_tasks", tasks.size());
            data.put("tasks", tasks);
            
            response.put("success", true);
            response.put("data", data);
            response.put("code", 200);
        } catch (Exception e) {
            logger.error("Error getting worker tasks", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }

    /**
     * POST /api/waste-tasks/{taskId}/cancel
     * Cancel a waste task
     */
    public Map<String, Object> cancelTask(Long taskId, String reason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<WasteTask> result = wasteManagementService.cancelTask(taskId, reason);
            
            if (result.isPresent()) {
                response.put("success", true);
                response.put("message", "Task cancelled successfully");
                response.put("code", 200);
            } else {
                response.put("success", false);
                response.put("message", "Task not found");
                response.put("code", 404);
            }
        } catch (Exception e) {
            logger.error("Error cancelling task", e);
            response.put("success", false);
            response.put("message", "Server error: " + e.getMessage());
            response.put("code", 500);
        }
        
        return response;
    }
}
