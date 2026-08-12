package com.waste.management.util;

import com.waste.management.entity.AttendanceRecord;
import com.waste.management.entity.WasteTask;
import com.waste.management.entity.Worker;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for exporting data to CSV format
 */
public class CsvExporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export workers to CSV
     */
    public static void exportWorkersToCsv(List<Worker> workers, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("Employee ID,Full Name,Email,Phone Number,Role,Department,Active,Created At\n");

            // Write data
            for (Worker worker : workers) {
                writer.append(worker.getEmployeeId()).append(",");
                writer.append(worker.getFullName()).append(",");
                writer.append(worker.getEmail()).append(",");
                writer.append(worker.getPhoneNumber()).append(",");
                writer.append(worker.getRole().getDisplayName()).append(",");
                writer.append(worker.getDepartment()).append(",");
                writer.append(String.valueOf(worker.isActive())).append(",");
                writer.append(worker.getCreatedAt().format(DATE_FORMATTER)).append("\n");
            }
        }
    }

    /**
     * Export attendance records to CSV
     */
    public static void exportAttendanceToCsv(List<AttendanceRecord> records, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("Worker ID,Worker Name,Check-In Time,Check-Out Time,Status,Face Match Confidence,Latitude,Longitude\n");

            // Write data
            for (AttendanceRecord record : records) {
                writer.append(String.valueOf(record.getWorker().getId())).append(",");
                writer.append(record.getWorker().getFullName()).append(",");
                writer.append(record.getCheckInTime().format(DATE_FORMATTER)).append(",");
                writer.append(record.getCheckOutTime() != null ? 
                    record.getCheckOutTime().format(DATE_FORMATTER) : "").append(",");
                writer.append(record.getStatus().getDisplayName()).append(",");
                writer.append(String.valueOf(record.getFaceMatchConfidence())).append(",");
                writer.append(record.getLocationLatitude() != null ? 
                    String.valueOf(record.getLocationLatitude()) : "").append(",");
                writer.append(record.getLocationLongitude() != null ? 
                    String.valueOf(record.getLocationLongitude()) : "").append("\n");
            }
        }
    }

    /**
     * Export waste tasks to CSV
     */
    public static void exportWasteTasksToCsv(List<WasteTask> tasks, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("Task ID,Area,Waste Type,Estimated Weight,Actual Weight,Status,Assigned Worker,Driver,Scheduled Date,Completion Date\n");

            // Write data
            for (WasteTask task : tasks) {
                writer.append(task.getTaskId()).append(",");
                writer.append(task.getArea()).append(",");
                writer.append(task.getWasteType()).append(",");
                writer.append(String.valueOf(task.getEstimatedWeight())).append(",");
                writer.append(String.valueOf(task.getActualWeight())).append(",");
                writer.append(task.getStatus().getDisplayName()).append(",");
                writer.append(task.getAssignedWorker() != null ? 
                    task.getAssignedWorker().getFullName() : "").append(",");
                writer.append(task.getDriver() != null ? 
                    task.getDriver().getFullName() : "").append(",");
                writer.append(task.getScheduledDate().format(DATE_FORMATTER)).append(",");
                writer.append(task.getCompletionDate() != null ? 
                    task.getCompletionDate().format(DATE_FORMATTER) : "").append("\n");
            }
        }
    }
}
