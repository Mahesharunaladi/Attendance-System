package com.waste.management.dto;

/**
 * Data Transfer Object for Waste Task Response
 */
public class WasteTaskDto {
    private Long id;
    private String taskId;
    private String area;
    private String wasteType;
    private Double estimatedWeight;
    private Double actualWeight;
    private String status;
    private String assignedWorkerName;
    private String driverName;
    private Double latitude;
    private Double longitude;
    private String notes;

    // Constructors
    public WasteTaskDto() {}

    public WasteTaskDto(Long id, String taskId, String area, String wasteType,
                       Double estimatedWeight, Double actualWeight, String status,
                       String assignedWorkerName, String driverName, 
                       Double latitude, Double longitude, String notes) {
        this.id = id;
        this.taskId = taskId;
        this.area = area;
        this.wasteType = wasteType;
        this.estimatedWeight = estimatedWeight;
        this.actualWeight = actualWeight;
        this.status = status;
        this.assignedWorkerName = assignedWorkerName;
        this.driverName = driverName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }

    public Double getEstimatedWeight() { return estimatedWeight; }
    public void setEstimatedWeight(Double estimatedWeight) { this.estimatedWeight = estimatedWeight; }

    public Double getActualWeight() { return actualWeight; }
    public void setActualWeight(Double actualWeight) { this.actualWeight = actualWeight; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedWorkerName() { return assignedWorkerName; }
    public void setAssignedWorkerName(String assignedWorkerName) { this.assignedWorkerName = assignedWorkerName; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
