package com.waste.management.example;

import com.waste.management.entity.WorkerRole;
import com.waste.management.service.FaceRecognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Example usage of the unified FaceRecognitionService
 * with role-specific models for Drivers and Workers
 */
public class FaceRecognitionExample {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionExample.class);
    
    private final FaceRecognitionService faceService;
    
    public FaceRecognitionExample() {
        this.faceService = new FaceRecognitionService();
        logger.info("Face Recognition Service initialized");
    }
    
    /**
     * Example 1: Verify a driver using driver-specific model
     */
    public void verifyDriver() {
        logger.info("=== Example 1: Driver Verification ===");
        
        // Reference face for driver (stored during registration)
        String driverReferenceFace = "data/faces/drivers/driver_john_smith.jpg";
        
        // Captured face during check-in
        String capturedFace = "data/faces/captured/check_in_driver_001.jpg";
        
        // Compare using DRIVER role (threshold: 0.80)
        double similarity = faceService.compareFacesByRole(
            driverReferenceFace,
            capturedFace,
            WorkerRole.DRIVER
        );
        
        logger.info("Driver similarity score: {}", String.format("%.4f", similarity));
        
        // Check with driver-specific threshold (0.80)
        boolean isMatch = faceService.isMatchConfidentForRole(similarity, WorkerRole.DRIVER);
        logger.info("Driver recognized: {}", isMatch);
        
        if (isMatch) {
            logger.info("✓ Driver John Smith check-in successful");
        } else {
            logger.info("✗ Driver verification failed - confidence too low");
        }
    }
    
    /**
     * Example 2: Verify a cleaner/worker using worker-specific model
     */
    public void verifyWorker() {
        logger.info("=== Example 2: Worker Verification ===");
        
        // Reference face for cleaner
        String workerReferenceFace = "data/faces/cleaners/cleaner_alice_johnson.jpg";
        
        // Captured face
        String capturedFace = "data/faces/captured/check_in_cleaner_002.jpg";
        
        // Compare using CLEANER role (threshold: 0.75)
        double similarity = faceService.compareFacesByRole(
            workerReferenceFace,
            capturedFace,
            WorkerRole.CLEANER
        );
        
        logger.info("Cleaner similarity score: {}", String.format("%.4f", similarity));
        
        // Check with cleaner-specific threshold (0.75)
        boolean isMatch = faceService.isMatchConfidentForRole(similarity, WorkerRole.CLEANER);
        logger.info("Cleaner recognized: {}", isMatch);
        
        if (isMatch) {
            logger.info("✓ Cleaner Alice Johnson check-in successful");
        } else {
            logger.info("✗ Cleaner verification failed - confidence too low");
        }
    }
    
    /**
     * Example 3: Multi-face verification for drivers
     */
    public void verifyMultipleDrivers() {
        logger.info("=== Example 3: Multi-Driver Verification ===");
        
        // Multiple driver reference faces
        List<String> driverFaces = Arrays.asList(
            "data/faces/drivers/driver_john_smith.jpg",
            "data/faces/drivers/driver_sarah_williams.jpg",
            "data/faces/drivers/driver_mike_johnson.jpg"
        );
        
        // Captured face during check-in
        String capturedFace = "data/faces/captured/check_in_driver_batch.jpg";
        
        // Verify against all drivers using DRIVER role
        FaceRecognitionService.FaceVerificationResult result = 
            faceService.verifyWorkerFromImageByRole(
                capturedFace,
                driverFaces,
                WorkerRole.DRIVER
            );
        
        logger.info("Verification result: {}", result);
        
        if (result.isMatched()) {
            logger.info("✓ Driver matched at index: {}", result.getMatchedIndex());
            logger.info("  Best similarity: {}", String.format("%.4f", result.getBestSimilarity()));
            logger.info("  Threshold: {}", String.format("%.4f", result.getThreshold()));
        } else {
            logger.info("✗ No matching driver found");
            logger.info("  Best similarity: {}", String.format("%.4f", result.getBestSimilarity()));
            logger.info("  Required threshold: {}", String.format("%.4f", result.getThreshold()));
        }
    }
    
    /**
     * Example 4: Multi-worker verification
     */
    public void verifyMultipleWorkers() {
        logger.info("=== Example 4: Multi-Worker Verification ===");
        
        // Multiple worker reference faces
        List<String> workerFaces = Arrays.asList(
            "data/faces/workers/cleaner_alice.jpg",
            "data/faces/workers/cleaner_bob.jpg",
            "data/faces/workers/helper_charlie.jpg"
        );
        
        // Captured face
        String capturedFace = "data/faces/captured/check_in_worker_batch.jpg";
        
        // Verify against all workers using CLEANER role
        FaceRecognitionService.FaceVerificationResult result = 
            faceService.verifyWorkerFromImageByRole(
                capturedFace,
                workerFaces,
                WorkerRole.CLEANER
            );
        
        logger.info("Worker verification result: {}", result);
        
        if (result.isMatched()) {
            logger.info("✓ Worker matched at index: {}", result.getMatchedIndex());
        } else {
            logger.info("✗ No matching worker found");
        }
    }
    
    /**
     * Example 5: Get threshold for different roles
     */
    public void demonstrateThresholds() {
        logger.info("=== Example 5: Role-Specific Thresholds ===");
        
        logger.info("DRIVER threshold: {}", String.format("%.2f", faceService.getThresholdForRole(WorkerRole.DRIVER)));
        logger.info("CLEANER threshold: {}", String.format("%.2f", faceService.getThresholdForRole(WorkerRole.CLEANER)));
        logger.info("HELPER threshold: {}", String.format("%.2f", faceService.getThresholdForRole(WorkerRole.HELPER)));
        logger.info("SUPERVISOR threshold: {}", String.format("%.2f", faceService.getThresholdForRole(WorkerRole.SUPERVISOR)));
        logger.info("MANAGER threshold: {}", String.format("%.2f", faceService.getThresholdForRole(WorkerRole.MANAGER)));
    }
    
    /**
     * Example 6: Print recognition model statistics
     */
    public void printModelStatistics() {
        logger.info("=== Example 6: Model Statistics ===");
        faceService.printAllModelStatistics();
    }
    
    /**
     * Example 7: Get specific model information
     */
    public void getModelInfo() {
        logger.info("=== Example 7: Model Information ===");
        
        // Get DRIVER model
        FaceRecognitionService.RecognitionModel driverModel = 
            faceService.getModelForRole(WorkerRole.DRIVER);
        
        if (driverModel != null) {
            logger.info("Driver Model:");
            logger.info("  Name: {}", driverModel.getModelName());
            logger.info("  Threshold: {}", String.format("%.2f", driverModel.getThreshold()));
            logger.info("  Trained Faces: {}", driverModel.getTrainedFaceCount());
            logger.info("  Total Comparisons: {}", driverModel.getTotalComparisons());
            logger.info("  Successful Matches: {}", driverModel.getSuccessfulMatches());
            logger.info("  Accuracy: {}%", String.format("%.2f", driverModel.getAccuracy()));
        }
        
        // Get CLEANER model
        FaceRecognitionService.RecognitionModel cleanerModel = 
            faceService.getModelForRole(WorkerRole.CLEANER);
        
        if (cleanerModel != null) {
            logger.info("Cleaner Model:");
            logger.info("  Name: {}", cleanerModel.getModelName());
            logger.info("  Threshold: {}", String.format("%.2f", cleanerModel.getThreshold()));
            logger.info("  Trained Faces: {}", cleanerModel.getTrainedFaceCount());
            logger.info("  Total Comparisons: {}", cleanerModel.getTotalComparisons());
            logger.info("  Successful Matches: {}", cleanerModel.getSuccessfulMatches());
            logger.info("  Accuracy: {}%", String.format("%.2f", cleanerModel.getAccuracy()));
        }
    }
    
    /**
     * Example 8: Detect faces in image
     */
    public void detectFacesExample() {
        logger.info("=== Example 8: Face Detection ===");
        
        String imagePath = "data/faces/group_photo.jpg";
        var faces = faceService.detectFaces(imagePath);
        
        logger.info("Detected {} faces in image", faces.size());
        for (int i = 0; i < faces.size(); i++) {
            var face = faces.get(i);
            logger.info("Face {}: x={}, y={}, width={}, height={}", 
                i, face.x, face.y, face.width, face.height);
        }
    }
    
    /**
     * Example 9: Comparison between driver and worker thresholds
     */
    public void compareThresholds() {
        logger.info("=== Example 9: Threshold Comparison ===");
        
        String referenceFace = "data/faces/test_face.jpg";
        String capturedFace = "data/faces/captured_face.jpg";
        
        // Get similarity score (same for all roles)
        double similarity = faceService.compareFaces(referenceFace, capturedFace);
        logger.info("Face similarity score: {}", String.format("%.4f", similarity));
        
        // Check against different thresholds
        boolean matchDriver = faceService.isMatchConfidentForRole(similarity, WorkerRole.DRIVER);
        boolean matchCleaner = faceService.isMatchConfidentForRole(similarity, WorkerRole.CLEANER);
        boolean matchSupervisor = faceService.isMatchConfidentForRole(similarity, WorkerRole.SUPERVISOR);
        
        logger.info("Matches DRIVER (0.80)?: {}", matchDriver);
        logger.info("Matches CLEANER (0.75)?: {}", matchCleaner);
        logger.info("Matches SUPERVISOR (0.78)?: {}", matchSupervisor);
        
        logger.info("\nThis demonstrates how the same face similarity score");
        logger.info("can have different results based on worker role thresholds.");
    }
    
    /**
     * Main method to run all examples
     */
    public static void main(String[] args) {
        FaceRecognitionExample example = new FaceRecognitionExample();
        
        try {
            // Run examples
            example.demonstrateThresholds();
            example.printModelStatistics();
            example.compareThresholds();
            example.getModelInfo();
            
            // Uncomment to run actual verification examples
            // (requires actual image files in specified paths)
            // example.verifyDriver();
            // example.verifyWorker();
            // example.verifyMultipleDrivers();
            // example.verifyMultipleWorkers();
            // example.detectFacesExample();
            
            logger.info("\n✓ All examples completed successfully!");
        } catch (Exception e) {
            logger.error("Error running examples", e);
        }
    }
}
