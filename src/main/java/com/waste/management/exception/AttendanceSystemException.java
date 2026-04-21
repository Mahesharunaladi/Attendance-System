package com.waste.management.exception;

/**
 * Custom exception for Attendance System
 */
public class AttendanceSystemException extends RuntimeException {
    private int errorCode;
    private String errorMessage;

    public AttendanceSystemException(String message) {
        super(message);
        this.errorMessage = message;
        this.errorCode = 500;
    }

    public AttendanceSystemException(String message, int errorCode) {
        super(message);
        this.errorMessage = message;
        this.errorCode = errorCode;
    }

    public AttendanceSystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
        this.errorCode = 500;
    }

    public AttendanceSystemException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
