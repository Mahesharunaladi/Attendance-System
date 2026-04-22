package com.waste.management.service;

import com.waste.management.entity.WorkerRole;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Role-Based Face Recognition Service
 * Maintains separate face recognition models for different worker roles (Driver, Worker, etc.)
 * Optimizes recognition accuracy by applying role-specific thresholds and configurations
 */
public class RoleBasedFaceRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(RoleBasedFaceRecognitionService.class);
    
    // Role-specific configurations
    private static final double DRIVER_MATCH_THRESHOLD = 0.80;      // Stricter threshold for drivers
    @SuppressWarnings("unused")
    private static final double WORKER_MATCH_THRESHOLD = 0.75;      // Standard threshold for other workers
    private static final double CLEANER_MATCH_THRESHOLD = 0.75;
    private static final double HELPER_MATCH_THRESHOLD = 0.75;
    private static final double SUPERVISOR_MATCH_THRESHOLD = 0.78;  // Slightly stricter for supervisors
    private static final double MANAGER_MATCH_THRESHOLD = 0.78;     // Slightly stricter for managers
    
    private static final String HAAR_CASCADE_PATH = "haarcascade_frontalface_alt.xml";
    @SuppressWarnings("unused")
    private static final String DRIVER_MODEL_PATH = "models/driver_face_model.dat";
    @SuppressWarnings("unused")
    private static final String WORKER_MODEL_PATH = "models/worker_face_model.dat";
    
    private CascadeClassifier faceDetector;
    
    // Face model storage for each role
    private Map<WorkerRole, FaceModel> roleModels;
    
    static {
        try {
            // Load OpenCV native library
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError | ExceptionInInitializerError e) {
            logger.warn("OpenCV native library not available. Some face recognition features may not work.", e);
        }
    }
    
    public RoleBasedFaceRecognitionService() {
        try {
            this.faceDetector = new CascadeClassifier(HAAR_CASCADE_PATH);
            if (faceDetector.empty()) {
                logger.error("Failed to load cascade classifier from path: {}", HAAR_CASCADE_PATH);
            }
            this.roleModels = new HashMap<>();
            initializeRoleModels();
        } catch (Exception e) {
            logger.error("Error initializing RoleBasedFaceRecognitionService", e);
        }
    }
    
    /**
     * Initialize face recognition models for each role
     */
    private void initializeRoleModels() {
        roleModels.put(WorkerRole.DRIVER, new FaceModel(
            WorkerRole.DRIVER, 
            DRIVER_MATCH_THRESHOLD,
            "Driver Face Recognition Model"
        ));
        
        roleModels.put(WorkerRole.CLEANER, new FaceModel(
            WorkerRole.CLEANER,
            CLEANER_MATCH_THRESHOLD,
            "Cleaner Face Recognition Model"
        ));
        
        roleModels.put(WorkerRole.HELPER, new FaceModel(
            WorkerRole.HELPER,
            HELPER_MATCH_THRESHOLD,
            "Helper Face Recognition Model"
        ));
        
        roleModels.put(WorkerRole.SUPERVISOR, new FaceModel(
            WorkerRole.SUPERVISOR,
            SUPERVISOR_MATCH_THRESHOLD,
            "Supervisor Face Recognition Model"
        ));
        
        roleModels.put(WorkerRole.MANAGER, new FaceModel(
            WorkerRole.MANAGER,
            MANAGER_MATCH_THRESHOLD,
            "Manager Face Recognition Model"
        ));
        
        logger.info("Initialized {} role-based face recognition models", roleModels.size());
    }
    
    /**
     * Detect faces in an image
     *
     * @param imagePath Path to the image file
     * @return List of detected faces as Rect
     */
    public List<Rect> detectFaces(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            logger.error("Failed to load image from path: {}", imagePath);
            return new ArrayList<>();
        }

        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        MatOfRect detectedFaces = new MatOfRect();
        faceDetector.detectMultiScale(grayImage, detectedFaces, 1.1, 4, 0, new Size(30, 30), new Size(500, 500));

        List<Rect> faceList = new ArrayList<>();
        for (Rect rect : detectedFaces.toArray()) {
            faceList.add(rect);
        }

        image.release();
        grayImage.release();

        logger.debug("Detected {} faces in image", faceList.size());
        return faceList;
    }
    
    /**
     * Extract facial features from detected face region
     *
     * @param imagePath Path to the image file
     * @param faceRegion The detected face region (Rect)
     * @return Mat containing the face region
     */
    public Mat extractFaceRegion(String imagePath, Rect faceRegion) {
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            logger.error("Failed to load image from path: {}", imagePath);
            return new Mat();
        }

        Mat faceROI = new Mat(image, faceRegion);
        Mat faceFeatures = new Mat();
        faceROI.copyTo(faceFeatures);

        image.release();
        faceROI.release();

        return faceFeatures;
    }
    
    /**
     * Compare two face images using role-specific threshold
     *
     * @param face1Path Path to first face image (reference)
     * @param face2Path Path to second face image (captured)
     * @param workerRole The role of the worker for role-specific threshold
     * @return Similarity score between 0 and 1
     */
    public double compareFacesByRole(String face1Path, String face2Path, WorkerRole workerRole) {
        Mat face1 = Imgcodecs.imread(face1Path);
        Mat face2 = Imgcodecs.imread(face2Path);

        if (face1.empty() || face2.empty()) {
            logger.error("Failed to load images for comparison");
            return 0.0;
        }

        // Resize both images to same size for comparison
        Mat face1Resized = new Mat();
        Mat face2Resized = new Mat();

        Imgproc.resize(face1, face1Resized, new Size(224, 224));
        Imgproc.resize(face2, face2Resized, new Size(224, 224));

        // Convert to grayscale
        Mat face1Gray = new Mat();
        Mat face2Gray = new Mat();

        Imgproc.cvtColor(face1Resized, face1Gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(face2Resized, face2Gray, Imgproc.COLOR_BGR2GRAY);

        // Calculate histogram
        Mat hist1 = computeHistogram(face1Gray);
        Mat hist2 = computeHistogram(face2Gray);

        // Compare histograms using Chi-Square distance
        double similarity = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_BHATTACHARYYA);

        // Normalize to 0-1 range (inverse of distance)
        similarity = 1.0 / (1.0 + similarity);

        logger.debug("Face similarity for role {}: {}", workerRole.getDisplayName(), similarity);

        face1.release();
        face2.release();
        face1Resized.release();
        face2Resized.release();
        face1Gray.release();
        face2Gray.release();
        hist1.release();
        hist2.release();

        return similarity;
    }
    
    /**
     * Verify if face matches for a specific role with role-specific threshold
     *
     * @param capturedImagePath Path to captured image
     * @param referenceFacePath Path to reference face image
     * @param workerRole The role of the worker
     * @return Recognition result with confidence score
     */
    public FaceRecognitionResult verifyFaceByRole(String capturedImagePath, String referenceFacePath, WorkerRole workerRole) {
        double similarity = compareFacesByRole(capturedImagePath, referenceFacePath, workerRole);
        double threshold = getThresholdForRole(workerRole);
        
        boolean isMatch = similarity >= threshold;
        
        logger.info("Face verification for {}: similarity={}, threshold={}, match={}",
                workerRole.getDisplayName(), String.format("%.4f", similarity), 
                String.format("%.4f", threshold), isMatch);
        
        return new FaceRecognitionResult(
            isMatch,
            similarity,
            threshold,
            workerRole,
            isMatch ? "Face recognized successfully" : "Face did not match threshold"
        );
    }
    
    /**
     * Verify worker from captured image against multiple reference faces (role-specific)
     *
     * @param capturedImagePath Path to captured image
     * @param referenceFacePaths List of reference face paths
     * @param workerRole The role of the worker
     * @return Best match index and confidence, or -1 if no match
     */
    public MultipleMatchResult verifyWorkerFromImageByRole(String capturedImagePath, 
                                                           List<String> referenceFacePaths, 
                                                           WorkerRole workerRole) {
        List<Rect> detectedFaces = detectFaces(capturedImagePath);
        if (detectedFaces.isEmpty()) {
            logger.warn("No faces detected in image for role: {}", workerRole.getDisplayName());
            return new MultipleMatchResult(-1, 0.0, getThresholdForRole(workerRole), false);
        }

        double bestMatch = 0.0;
        int bestMatchIndex = -1;
        double threshold = getThresholdForRole(workerRole);

        for (int i = 0; i < referenceFacePaths.size(); i++) {
            double similarity = compareFacesByRole(capturedImagePath, referenceFacePaths.get(i), workerRole);
            if (similarity > bestMatch) {
                bestMatch = similarity;
                bestMatchIndex = i;
            }
        }

        boolean isMatched = bestMatch >= threshold;
        
        logger.info("Worker verification for role {}: bestMatch={}, threshold={}, matchedIndex={}",
                workerRole.getDisplayName(), String.format("%.4f", bestMatch),
                String.format("%.4f", threshold), bestMatchIndex);
        
        return new MultipleMatchResult(bestMatchIndex, bestMatch, threshold, isMatched);
    }
    
    /**
     * Compute histogram for a grayscale image
     *
     * @param grayImage Grayscale image
     * @return Histogram as Mat
     */
    private Mat computeHistogram(Mat grayImage) {
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0, 256);
        MatOfInt channels = new MatOfInt(0);

        Imgproc.calcHist(
                java.util.Arrays.asList(grayImage),
                channels,
                new Mat(),
                hist,
                histSize,
                ranges
        );

        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);

        histSize.release();
        ranges.release();
        channels.release();

        return hist;
    }
    
    /**
     * Get threshold for a specific worker role
     *
     * @param role Worker role
     * @return Threshold value (0-1)
     */
    public double getThresholdForRole(WorkerRole role) {
        return switch (role) {
            case DRIVER -> DRIVER_MATCH_THRESHOLD;
            case CLEANER -> CLEANER_MATCH_THRESHOLD;
            case HELPER -> HELPER_MATCH_THRESHOLD;
            case SUPERVISOR -> SUPERVISOR_MATCH_THRESHOLD;
            case MANAGER -> MANAGER_MATCH_THRESHOLD;
            default -> 0.75;
        };
    }
    
    /**
     * Get face model for a specific role
     *
     * @param role Worker role
     * @return Face model for the role
     */
    public FaceModel getFaceModelForRole(WorkerRole role) {
        return roleModels.get(role);
    }
    
    /**
     * Get all role-based models
     *
     * @return Map of all role models
     */
    public Map<WorkerRole, FaceModel> getAllRoleModels() {
        return new HashMap<>(roleModels);
    }
    
    /**
     * Get model statistics for a specific role
     *
     * @param role Worker role
     * @return Model statistics
     */
    public String getModelStatistics(WorkerRole role) {
        FaceModel model = roleModels.get(role);
        if (model == null) {
            return "Model not found for role: " + role.getDisplayName();
        }
        return model.getStatistics();
    }
    
    /**
     * Print all role model statistics
     */
    public void printAllModelStatistics() {
        logger.info("===== Role-Based Face Recognition Models Statistics =====");
        roleModels.forEach((role, model) -> {
            logger.info("{}: {}", role.getDisplayName(), model.getStatistics());
        });
        logger.info("=========================================================");
    }
    
    /**
     * Inner class to represent a face recognition model for a specific role
     */
    public static class FaceModel {
        private final WorkerRole role;
        private final double threshold;
        private final String modelName;
        private long trainedFaceCount;
        private long totalComparisons;
        private long successfulMatches;
        
        public FaceModel(WorkerRole role, double threshold, String modelName) {
            this.role = role;
            this.threshold = threshold;
            this.modelName = modelName;
            this.trainedFaceCount = 0;
            this.totalComparisons = 0;
            this.successfulMatches = 0;
        }
        
        public WorkerRole getRole() { return role; }
        public double getThreshold() { return threshold; }
        public String getModelName() { return modelName; }
        public long getTrainedFaceCount() { return trainedFaceCount; }
        public long getTotalComparisons() { return totalComparisons; }
        public long getSuccessfulMatches() { return successfulMatches; }
        
        public void incrementTrainedFaceCount() { this.trainedFaceCount++; }
        public void incrementTotalComparisons() { this.totalComparisons++; }
        public void incrementSuccessfulMatches() { this.successfulMatches++; }
        
        public double getAccuracy() {
            return totalComparisons == 0 ? 0.0 : (double) successfulMatches / totalComparisons * 100;
        }
        
        public String getStatistics() {
            return String.format(
                "%s | Threshold: %.2f | Trained Faces: %d | Comparisons: %d | Successful: %d | Accuracy: %.2f%%",
                modelName, threshold, trainedFaceCount, totalComparisons, successfulMatches, getAccuracy()
            );
        }
    }
    
    /**
     * Result class for single face recognition
     */
    public static class FaceRecognitionResult {
        private final boolean matched;
        private final double similarity;
        private final double threshold;
        private final WorkerRole role;
        private final String message;
        
        public FaceRecognitionResult(boolean matched, double similarity, double threshold, 
                                     WorkerRole role, String message) {
            this.matched = matched;
            this.similarity = similarity;
            this.threshold = threshold;
            this.role = role;
            this.message = message;
        }
        
        public boolean isMatched() { return matched; }
        public double getSimilarity() { return similarity; }
        public double getThreshold() { return threshold; }
        public WorkerRole getRole() { return role; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format(
                "FaceRecognitionResult{role=%s, matched=%s, similarity=%.4f, threshold=%.4f, message='%s'}",
                role.getDisplayName(), matched, similarity, threshold, message
            );
        }
    }
    
    /**
     * Result class for multiple face matching
     */
    public static class MultipleMatchResult {
        private final int matchedIndex;
        private final double bestSimilarity;
        private final double threshold;
        private final boolean matched;
        
        public MultipleMatchResult(int matchedIndex, double bestSimilarity, double threshold, boolean matched) {
            this.matchedIndex = matchedIndex;
            this.bestSimilarity = bestSimilarity;
            this.threshold = threshold;
            this.matched = matched;
        }
        
        public int getMatchedIndex() { return matchedIndex; }
        public double getBestSimilarity() { return bestSimilarity; }
        public double getThreshold() { return threshold; }
        public boolean isMatched() { return matched; }
        
        @Override
        public String toString() {
            return String.format(
                "MultipleMatchResult{matchedIndex=%d, bestSimilarity=%.4f, threshold=%.4f, matched=%s}",
                matchedIndex, bestSimilarity, threshold, matched
            );
        }
    }
}
