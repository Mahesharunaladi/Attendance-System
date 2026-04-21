package com.waste.management.service;

import com.waste.management.dto.WorkerDetailsDto;
import com.waste.management.entity.Worker;
import com.waste.management.entity.WorkerRole;
import com.waste.management.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Enhanced Face Recognition Service with Worker Details Fetching
 * Recognizes faces and automatically fetches complete worker information
 */
public class FaceRecognitionWithDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionWithDetailsService.class);
    
    private final FaceRecognitionService faceRecognitionService;
    private final WorkerRepository workerRepository;

    public FaceRecognitionWithDetailsService(WorkerRepository workerRepository) {
        this.faceRecognitionService = new FaceRecognitionService();
        this.workerRepository = workerRepository;
    }

    /**
     * Recognize driver from captured image and fetch complete details
     *
     * @param capturedImagePath Path to captured image
     * @return WorkerDetailsDto with driver information and confidence score
     */
    public Optional<WorkerDetailsDto> recognizeDriverAndFetchDetails(String capturedImagePath) {
        logger.info("Recognizing driver from image: {}", capturedImagePath);

        // Get all drivers
        List<Worker> drivers = workerRepository.findByRole(WorkerRole.DRIVER);
        
        if (drivers.isEmpty()) {
            logger.warn("No drivers found in database");
            return Optional.empty();
        }

        // Get reference face paths for all drivers
        List<String> driverFacePaths = drivers.stream()
                .map(Worker::getFacialDataPath)
                .toList();

        // Perform multi-face verification with DRIVER role (0.80 threshold)
        FaceRecognitionService.FaceVerificationResult result = 
            faceRecognitionService.verifyWorkerFromImageByRole(
                capturedImagePath,
                driverFacePaths,
                WorkerRole.DRIVER
            );

        if (!result.isMatched()) {
            logger.warn("Driver face not recognized - confidence too low");
            return Optional.empty();
        }

        // Get the matched driver
        Worker matchedDriver = drivers.get(result.getMatchedIndex());
        logger.info("Driver recognized: {} (ID: {}) with confidence: {}",
                matchedDriver.getFullName(), matchedDriver.getEmployeeId(),
                String.format("%.4f", result.getBestSimilarity()));

        // Create and populate WorkerDetailsDto
        WorkerDetailsDto driverDetails = new WorkerDetailsDto(
                matchedDriver.getId(),
                matchedDriver.getEmployeeId(),
                matchedDriver.getFullName(),
                matchedDriver.getPhoneNumber(),
                matchedDriver.getEmail(),
                matchedDriver.getRole(),
                matchedDriver.getAadharNumber(),
                matchedDriver.getGender(),
                matchedDriver.getCaste(),
                matchedDriver.getDepartment(),
                matchedDriver.isActive()
        );

        driverDetails.setFaceMatchConfidence(result.getBestSimilarity());
        driverDetails.setMessage("Driver recognized successfully");

        return Optional.of(driverDetails);
    }

    /**
     * Recognize worker (cleaner, helper, etc.) from captured image and fetch complete details
     *
     * @param capturedImagePath Path to captured image
     * @param workerRole The role of worker to recognize
     * @return WorkerDetailsDto with worker information and confidence score
     */
    public Optional<WorkerDetailsDto> recognizeWorkerAndFetchDetails(String capturedImagePath, WorkerRole workerRole) {
        logger.info("Recognizing {} from image: {}", workerRole.getDisplayName(), capturedImagePath);

        // Get all workers with specified role
        List<Worker> workers = workerRepository.findByRole(workerRole);
        
        if (workers.isEmpty()) {
            logger.warn("No {} found in database", workerRole.getDisplayName());
            return Optional.empty();
        }

        // Get reference face paths
        List<String> workerFacePaths = workers.stream()
                .map(Worker::getFacialDataPath)
                .toList();

        // Perform multi-face verification with role-specific threshold
        FaceRecognitionService.FaceVerificationResult result = 
            faceRecognitionService.verifyWorkerFromImageByRole(
                capturedImagePath,
                workerFacePaths,
                workerRole
            );

        if (!result.isMatched()) {
            logger.warn("{} face not recognized - confidence too low", workerRole.getDisplayName());
            return Optional.empty();
        }

        // Get the matched worker
        Worker matchedWorker = workers.get(result.getMatchedIndex());
        logger.info("{} recognized: {} (ID: {}) with confidence: {}",
                workerRole.getDisplayName(), matchedWorker.getFullName(),
                matchedWorker.getEmployeeId(),
                String.format("%.4f", result.getBestSimilarity()));

        // Create and populate WorkerDetailsDto
        WorkerDetailsDto workerDetails = new WorkerDetailsDto(
                matchedWorker.getId(),
                matchedWorker.getEmployeeId(),
                matchedWorker.getFullName(),
                matchedWorker.getPhoneNumber(),
                matchedWorker.getEmail(),
                matchedWorker.getRole(),
                matchedWorker.getAadharNumber(),
                matchedWorker.getGender(),
                matchedWorker.getCaste(),
                matchedWorker.getDepartment(),
                matchedWorker.isActive()
        );

        workerDetails.setFaceMatchConfidence(result.getBestSimilarity());
        workerDetails.setMessage(workerRole.getDisplayName() + " recognized successfully");

        return Optional.of(workerDetails);
    }

    /**
     * Recognize any worker from captured image (auto-detect role) and fetch details
     * Tries all roles and returns the best match
     *
     * @param capturedImagePath Path to captured image
     * @return WorkerDetailsDto with worker information
     */
    public Optional<WorkerDetailsDto> recognizeAnyWorkerAndFetchDetails(String capturedImagePath) {
        logger.info("Recognizing any worker from image: {}", capturedImagePath);

        WorkerRole[] roles = {WorkerRole.DRIVER, WorkerRole.CLEANER, WorkerRole.HELPER,
                               WorkerRole.SUPERVISOR, WorkerRole.MANAGER};

        double bestOverallSimilarity = 0.0;
        Optional<WorkerDetailsDto> bestMatch = Optional.empty();

        // Try to recognize with each role
        for (WorkerRole role : roles) {
            Optional<WorkerDetailsDto> result = recognizeWorkerAndFetchDetails(capturedImagePath, role);
            
            if (result.isPresent()) {
                WorkerDetailsDto workerDetails = result.get();
                if (workerDetails.getFaceMatchConfidence() > bestOverallSimilarity) {
                    bestOverallSimilarity = workerDetails.getFaceMatchConfidence();
                    bestMatch = result;
                }
            }
        }

        if (bestMatch.isPresent()) {
            logger.info("Best match found: {} with confidence: {}",
                    bestMatch.get().getFullName(),
                    String.format("%.4f", bestOverallSimilarity));
        } else {
            logger.warn("No worker recognized from image");
        }

        return bestMatch;
    }

    /**
     * Get worker details by ID
     *
     * @param workerId Worker ID
     * @return WorkerDetailsDto with worker information
     */
    public Optional<WorkerDetailsDto> getWorkerDetailsById(Long workerId) {
        Optional<Worker> worker = workerRepository.findById(workerId);
        
        if (worker.isEmpty()) {
            logger.warn("Worker not found with ID: {}", workerId);
            return Optional.empty();
        }

        Worker w = worker.get();
        WorkerDetailsDto details = new WorkerDetailsDto(
                w.getId(),
                w.getEmployeeId(),
                w.getFullName(),
                w.getPhoneNumber(),
                w.getEmail(),
                w.getRole(),
                w.getAadharNumber(),
                w.getGender(),
                w.getCaste(),
                w.getDepartment(),
                w.isActive()
        );

        return Optional.of(details);
    }

    /**
     * Get worker details by Employee ID
     *
     * @param employeeId Employee ID
     * @return WorkerDetailsDto with worker information
     */
    public Optional<WorkerDetailsDto> getWorkerDetailsByEmployeeId(String employeeId) {
        Optional<Worker> worker = workerRepository.findByEmployeeId(employeeId);
        
        if (worker.isEmpty()) {
            logger.warn("Worker not found with Employee ID: {}", employeeId);
            return Optional.empty();
        }

        Worker w = worker.get();
        WorkerDetailsDto details = new WorkerDetailsDto(
                w.getId(),
                w.getEmployeeId(),
                w.getFullName(),
                w.getPhoneNumber(),
                w.getEmail(),
                w.getRole(),
                w.getAadharNumber(),
                w.getGender(),
                w.getCaste(),
                w.getDepartment(),
                w.isActive()
        );

        return Optional.of(details);
    }

    /**
     * Get all drivers with their details
     *
     * @return List of all driver details
     */
    public List<WorkerDetailsDto> getAllDriverDetails() {
        List<Worker> drivers = workerRepository.findByRole(WorkerRole.DRIVER);
        
        return drivers.stream()
                .map(w -> new WorkerDetailsDto(
                        w.getId(),
                        w.getEmployeeId(),
                        w.getFullName(),
                        w.getPhoneNumber(),
                        w.getEmail(),
                        w.getRole(),
                        w.getAadharNumber(),
                        w.getGender(),
                        w.getCaste(),
                        w.getDepartment(),
                        w.isActive()
                ))
                .toList();
    }

    /**
     * Print driver details in formatted way
     *
     * @param workerDetails Worker details to print
     */
    public void printWorkerDetails(WorkerDetailsDto workerDetails) {
        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║           WORKER DETAILS - FACE RECOGNITION               ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║ Worker ID: {}", String.format("%-48s║", workerDetails.getWorkerId()));
        logger.info("║ Employee ID: {}", String.format("%-46s║", workerDetails.getEmployeeId()));
        logger.info("║ Full Name: {}", String.format("%-47s║", workerDetails.getFullName()));
        logger.info("║ Phone Number: {}", String.format("%-44s║", workerDetails.getPhoneNumber()));
        logger.info("║ Email: {}", String.format("%-50s║", workerDetails.getEmail()));
        logger.info("║ Role: {}", String.format("%-51s║", workerDetails.getRole().getDisplayName()));
        logger.info("║ Aadhar Number: {}", String.format("%-43s║", workerDetails.getAadharNumber()));
        logger.info("║ Gender: {}", String.format("%-49s║", workerDetails.getGender().getDisplayName()));
        logger.info("║ Caste: {}", String.format("%-50s║", workerDetails.getCaste()));
        logger.info("║ Department: {}", String.format("%-45s║", workerDetails.getDepartment()));
        logger.info("║ Active: {}", String.format("%-50s║", workerDetails.isActive()));
        logger.info("║ Face Match Confidence: {}", String.format("%-38s║", String.format("%.4f", workerDetails.getFaceMatchConfidence())));
        logger.info("║ Message: {}", String.format("%-48s║", workerDetails.getMessage()));
        logger.info("╚════════════════════════════════════════════════════════════╝");
    }
}
