package com.waste.management.dto;

import com.waste.management.entity.Gender;
import com.waste.management.entity.WorkerRole;

/**
 * Data Transfer Object for returning worker details after face recognition
 */
public class WorkerDetailsDto {
    private Long workerId;
    private String employeeId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private WorkerRole role;
    private String aadharNumber;
    private Gender gender;
    private String caste;
    private String department;
    private boolean active;
    private double faceMatchConfidence;
    private String message;

    // Constructors
    public WorkerDetailsDto() {
    }

    public WorkerDetailsDto(Long workerId, String employeeId, String fullName, String phoneNumber,
                            String email, WorkerRole role, String aadharNumber, Gender gender,
                            String caste, String department, boolean active) {
        this.workerId = workerId;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
        this.aadharNumber = aadharNumber;
        this.gender = gender;
        this.caste = caste;
        this.department = department;
        this.active = active;
    }

    // Getters and Setters
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public WorkerRole getRole() { return role; }
    public void setRole(WorkerRole role) { this.role = role; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getCaste() { return caste; }
    public void setCaste(String caste) { this.caste = caste; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getFaceMatchConfidence() { return faceMatchConfidence; }
    public void setFaceMatchConfidence(double faceMatchConfidence) { this.faceMatchConfidence = faceMatchConfidence; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "WorkerDetailsDto{" +
                "workerId=" + workerId +
                ", employeeId='" + employeeId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", aadharNumber='" + aadharNumber + '\'' +
                ", gender=" + gender +
                ", caste='" + caste + '\'' +
                ", department='" + department + '\'' +
                ", active=" + active +
                ", faceMatchConfidence=" + faceMatchConfidence +
                ", message='" + message + '\'' +
                '}';
    }
}
