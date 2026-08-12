package com.waste.management.service;

import com.waste.management.entity.AttendanceRecord;
import com.waste.management.entity.AttendanceStatus;
import com.waste.management.entity.Worker;
import com.waste.management.repository.AttendanceRepository;
import com.waste.management.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Attendance Service for managing attendance records
 */
public class AttendanceService {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    private static final int LATE_THRESHOLD_MINUTES = 15;
    private static final LocalTime STANDARD_WORKING_START = LocalTime.of(9, 0);

    private final AttendanceRepository attendanceRepository;
    private final FaceRecognitionService faceRecognitionService;
    private final WorkerRepository workerRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, 
                            FaceRecognitionService faceRecognitionService,
                            WorkerRepository workerRepository) {
        this.attendanceRepository = attendanceRepository;
        this.faceRecognitionService = faceRecognitionService;
        this.workerRepository = workerRepository;
    }

    /**
     * Record attendance check-in for a worker via face recognition
     *
     * @param worker The worker checking in
     * @param imagePath Path to captured image
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @return AttendanceRecord if successful
     */
    public Optional<AttendanceRecord> recordCheckIn(Worker worker, String imagePath, 
                                                     Double latitude, Double longitude) {
        try {
            // Verify worker's face
            double faceConfidence = faceRecognitionService.compareFaces(imagePath, worker.getFacialDataPath());
            
            if (!faceRecognitionService.isMatchConfident(faceConfidence)) {
                logger.warn("Face verification failed for worker: {}", worker.getEmployeeId());
                return Optional.empty();
            }

            // Check if already checked in today
            Optional<AttendanceRecord> existingRecord = attendanceRepository
                    .findTodayCheckInByWorker(worker.getId(), LocalDate.now());
            if (existingRecord.isPresent()) {
                logger.info("Worker already checked in today: {}", worker.getEmployeeId());
                return Optional.empty();
            }

            // Create new attendance record
            AttendanceRecord record = new AttendanceRecord();
            record.setWorker(worker);
            record.setCheckInTime(LocalDateTime.now());
            record.setLocationLatitude(latitude);
            record.setLocationLongitude(longitude);
            record.setImageCapturedPath(imagePath);
            record.setFaceMatchConfidence(faceConfidence);

            // Determine attendance status
            LocalTime currentTime = LocalDateTime.now().toLocalTime();
            if (currentTime.isAfter(STANDARD_WORKING_START.plusMinutes(LATE_THRESHOLD_MINUTES))) {
                record.setStatus(AttendanceStatus.LATE);
            } else {
                record.setStatus(AttendanceStatus.PRESENT);
            }

            return Optional.of(attendanceRepository.save(record));
        } catch (Exception e) {
            logger.error("Error recording check-in for worker: {}", worker.getEmployeeId(), e);
            return Optional.empty();
        }
    }

    /**
     * Record attendance check-out for a worker
     *
     * @param worker The worker checking out
     * @param imagePath Path to captured image
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @return Updated AttendanceRecord
     */
    public Optional<AttendanceRecord> recordCheckOut(Worker worker, String imagePath, 
                                                      Double latitude, Double longitude) {
        try {
            // Verify worker's face
            double faceConfidence = faceRecognitionService.compareFaces(imagePath, worker.getFacialDataPath());
            
            if (!faceRecognitionService.isMatchConfident(faceConfidence)) {
                logger.warn("Face verification failed for worker: {}", worker.getEmployeeId());
                return Optional.empty();
            }

            // Find today's check-in record
            Optional<AttendanceRecord> record = attendanceRepository
                    .findTodayCheckInByWorker(worker.getId(), LocalDate.now());
            
            if (record.isEmpty()) {
                logger.warn("No check-in record found for worker: {}", worker.getEmployeeId());
                return Optional.empty();
            }

            AttendanceRecord attendanceRecord = record.get();
            attendanceRecord.setCheckOutTime(LocalDateTime.now());
            attendanceRecord.setImageCapturedPath(imagePath);
            attendanceRecord.setLocationLatitude(latitude);
            attendanceRecord.setLocationLongitude(longitude);

            return Optional.of(attendanceRepository.save(attendanceRecord));
        } catch (Exception e) {
            logger.error("Error recording check-out for worker: {}", worker.getEmployeeId(), e);
            return Optional.empty();
        }
    }

    /**
     * Get attendance report for a date range
     *
     * @param startDate Start date
     * @param endDate End date
     * @return List of attendance records
     */
    public List<AttendanceRecord> getAttendanceReport(LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByDateRange(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    /**
     * Get attendance report for specific worker
     *
     * @param workerId Worker ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of attendance records
     */
    public List<AttendanceRecord> getWorkerAttendanceReport(Long workerId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByWorkerAndDateRange(workerId, 
                                                            startDate.atStartOfDay(), 
                                                            endDate.atTime(23, 59, 59));
    }

    /**
     * Get today's attendance record for a worker
     *
     * @param worker The worker to check
     * @return Optional containing today's attendance record if present
     */
    public Optional<AttendanceRecord> getTodayAttendanceForWorker(Worker worker) {
        return attendanceRepository.findTodayCheckInByWorker(worker.getId(), LocalDate.now());
    }

    /**
     * Get today's attendance count
     *
     * @return Number of workers present today
     */
    public long getTodayAttendanceCount() {
        return attendanceRepository.countTodayCheckIns(LocalDate.now());
    }

    /**
     * Get absentee count for a date
     *
     * @param allWorkers All active workers
     * @param date Date to check
     * @return Count of absent workers
     */
    public long getAbsenteeCount(List<Worker> allWorkers, LocalDate date) {
        long presentCount = attendanceRepository.countTodayCheckIns(date);
        return allWorkers.size() - presentCount;
    }

    /**
     * Identify worker from captured face image
     * Compares the provided face image against all registered workers' facial data
     *
     * @param imagePath Path to captured face image
     * @return Optional containing the identified worker if found
     */
    public Optional<Worker> identifyWorkerFromFace(String imagePath) {
        try {
            // Get all active workers
            List<Worker> allWorkers = workerRepository.findAllActive();
            
            double highestConfidence = 0.0;
            Worker bestMatch = null;

            // Compare face against all workers
            for (Worker worker : allWorkers) {
                try {
                    double confidence = faceRecognitionService.compareFaces(imagePath, worker.getFacialDataPath());
                    
                    // Check if this is the best match so far
                    if (confidence > highestConfidence && faceRecognitionService.isMatchConfident(confidence)) {
                        highestConfidence = confidence;
                        bestMatch = worker;
                    }
                } catch (Exception e) {
                    logger.warn("Error comparing face for worker: {}", worker.getEmployeeId(), e);
                    // Continue to next worker
                }
            }

            if (bestMatch != null) {
                logger.info("Worker identified from face: {} (confidence: {})", bestMatch.getEmployeeId(), highestConfidence);
                return Optional.of(bestMatch);
            }

            logger.warn("No matching worker found for provided face");
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error identifying worker from face", e);
            return Optional.empty();
        }
    }
}
