package com.waste.management.service;

import com.waste.management.entity.WasteTask;
import com.waste.management.entity.TaskStatus;
import com.waste.management.entity.Worker;
import com.waste.management.repository.WasteTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Waste Management Service for managing waste collection tasks
 */
public class WasteManagementService {
    private static final Logger logger = LoggerFactory.getLogger(WasteManagementService.class);
    private static final double AVERAGE_WASTE_WEIGHT_KG = 50.0;

    private final WasteTaskRepository wasteTaskRepository;

    public WasteManagementService(WasteTaskRepository wasteTaskRepository) {
        this.wasteTaskRepository = wasteTaskRepository;
    }

    /**
     * Create a new waste collection task
     *
     * @param area Collection area
     * @param wasteType Type of waste (Organic, Inorganic, Hazardous, etc.)
     * @param estimatedWeight Estimated weight of waste
     * @return Created WasteTask
     */
    public WasteTask createWasteTask(String area, String wasteType, Double estimatedWeight) {
        WasteTask task = new WasteTask();
        task.setTaskId(generateTaskId());
        task.setArea(area);
        task.setWasteType(wasteType);
        task.setEstimatedWeight(estimatedWeight > 0 ? estimatedWeight : AVERAGE_WASTE_WEIGHT_KG);
        task.setActualWeight(0.0);
        task.setStatus(TaskStatus.PENDING);
        task.setScheduledDate(LocalDateTime.now());

        logger.info("Created waste task: {} for area: {}", task.getTaskId(), area);
        return wasteTaskRepository.save(task);
    }

    /**
     * Assign worker to a waste task
     *
     * @param taskId Task ID
     * @param worker Worker to assign
     * @return Updated WasteTask
     */
    public Optional<WasteTask> assignWorkerToTask(Long taskId, Worker worker) {
        Optional<WasteTask> task = wasteTaskRepository.findById(taskId);
        if (task.isEmpty()) {
            logger.warn("Task not found: {}", taskId);
            return Optional.empty();
        }

        WasteTask wasteTask = task.get();
        wasteTask.setAssignedWorker(worker);
        wasteTask.setStatus(TaskStatus.IN_PROGRESS);

        logger.info("Assigned worker {} to task {}", worker.getEmployeeId(), taskId);
        return Optional.of(wasteTaskRepository.save(wasteTask));
    }

    /**
     * Assign driver to a waste collection vehicle/route
     *
     * @param taskId Task ID
     * @param driver Driver to assign
     * @return Updated WasteTask
     */
    public Optional<WasteTask> assignDriverToTask(Long taskId, Worker driver) {
        Optional<WasteTask> task = wasteTaskRepository.findById(taskId);
        if (task.isEmpty()) {
            logger.warn("Task not found: {}", taskId);
            return Optional.empty();
        }

        WasteTask wasteTask = task.get();
        wasteTask.setDriver(driver);

        logger.info("Assigned driver {} to task {}", driver.getEmployeeId(), taskId);
        return Optional.of(wasteTaskRepository.save(wasteTask));
    }

    /**
     * Update task with actual weight and completion details
     *
     * @param taskId Task ID
     * @param actualWeight Actual weight collected
     * @param latitude GPS latitude of completion
     * @param longitude GPS longitude of completion
     * @return Updated WasteTask
     */
    public Optional<WasteTask> completeTask(Long taskId, Double actualWeight, Double latitude, Double longitude) {
        Optional<WasteTask> task = wasteTaskRepository.findById(taskId);
        if (task.isEmpty()) {
            logger.warn("Task not found: {}", taskId);
            return Optional.empty();
        }

        WasteTask wasteTask = task.get();
        wasteTask.setActualWeight(actualWeight);
        wasteTask.setCompletionDate(LocalDateTime.now());
        wasteTask.setLocationLatitude(latitude);
        wasteTask.setLocationLongitude(longitude);
        wasteTask.setStatus(TaskStatus.COMPLETED);

        logger.info("Completed task: {} with weight: {} kg", taskId, actualWeight);
        return Optional.of(wasteTaskRepository.save(wasteTask));
    }

    /**
     * Get pending tasks
     *
     * @return List of pending tasks
     */
    public List<WasteTask> getPendingTasks() {
        return wasteTaskRepository.findByStatus(TaskStatus.PENDING);
    }

    /**
     * Get in-progress tasks
     *
     * @return List of in-progress tasks
     */
    public List<WasteTask> getInProgressTasks() {
        return wasteTaskRepository.findByStatus(TaskStatus.IN_PROGRESS);
    }

    /**
     * Get tasks assigned to specific worker
     *
     * @param workerId Worker ID
     * @return List of assigned tasks
     */
    public List<WasteTask> getTasksAssignedToWorker(Long workerId) {
        return wasteTaskRepository.findByAssignedWorker(workerId);
    }

    /**
     * Get tasks assigned to specific driver
     *
     * @param driverId Driver ID
     * @return List of assigned tasks
     */
    public List<WasteTask> getTasksAssignedToDriver(Long driverId) {
        return wasteTaskRepository.findByDriver(driverId);
    }

    /**
     * Get completion statistics
     *
     * @return Array with [totalTasks, completedTasks]
     */
    public long[] getCompletionStatistics() {
        long totalTasks = wasteTaskRepository.countTotal();
        long completedTasks = wasteTaskRepository.countByStatus(TaskStatus.COMPLETED);
        return new long[]{totalTasks, completedTasks};
    }

    /**
     * Get total waste collected
     *
     * @return Total weight in kg
     */
    public Double getTotalWasteCollected() {
        return wasteTaskRepository.sumActualWeight();
    }

    /**
     * Generate unique task ID
     *
     * @return Generated task ID
     */
    private String generateTaskId() {
        return "TASK-" + System.currentTimeMillis();
    }

    /**
     * Cancel a waste task
     *
     * @param taskId Task ID
     * @param reason Reason for cancellation
     * @return Updated WasteTask
     */
    public Optional<WasteTask> cancelTask(Long taskId, String reason) {
        Optional<WasteTask> task = wasteTaskRepository.findById(taskId);
        if (task.isEmpty()) {
            logger.warn("Task not found: {}", taskId);
            return Optional.empty();
        }

        WasteTask wasteTask = task.get();
        wasteTask.setStatus(TaskStatus.CANCELLED);
        wasteTask.setNotes(reason);

        logger.info("Cancelled task: {} with reason: {}", taskId, reason);
        return Optional.of(wasteTaskRepository.save(wasteTask));
    }
}
