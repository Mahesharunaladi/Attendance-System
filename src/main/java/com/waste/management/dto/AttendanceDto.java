package com.waste.management.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Attendance Response
 */
public class AttendanceDto {
    private Long recordId;
    private String employeeId;
    private String workerName;
    private String status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Double faceMatchConfidence;
    private Double latitude;
    private Double longitude;

    // Constructors
    public AttendanceDto() {}

    public AttendanceDto(Long recordId, String employeeId, String workerName, String status,
                        LocalDateTime checkInTime, LocalDateTime checkOutTime, 
                        Double faceMatchConfidence, Double latitude, Double longitude) {
        this.recordId = recordId;
        this.employeeId = employeeId;
        this.workerName = workerName;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.faceMatchConfidence = faceMatchConfidence;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Double getFaceMatchConfidence() { return faceMatchConfidence; }
    public void setFaceMatchConfidence(Double faceMatchConfidence) { this.faceMatchConfidence = faceMatchConfidence; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
