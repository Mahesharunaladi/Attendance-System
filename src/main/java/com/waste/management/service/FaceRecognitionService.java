package com.waste.management.service;

import com.waste.management.entity.WorkerRole;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Unified Face Recognition Service using OpenCV for detecting and matching faces.
 * Supports role-specific recognition for Drivers and Workers with optimized thresholds.
 */
public class FaceRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionService.class);
    
    // Default threshold for general workers
    private static final double MATCH_THRESHOLD = 0.75;
    
    // Role-specific thresholds for optimized recognition
    private static final double DRIVER_MATCH_THRESHOLD = 0.80;      // Stricter for drivers
    private static final double CLEANER_MATCH_THRESHOLD = 0.75;
    private static final double HELPER_MATCH_THRESHOLD = 0.75;
    private static final double SUPERVISOR_MATCH_THRESHOLD = 0.78;
    private static final double MANAGER_MATCH_THRESHOLD = 0.78;
    
    private static final String HAAR_CASCADE_PATH = "haarcascade_frontalface_alt.xml";

    private CascadeClassifier faceDetector;
    private Map<WorkerRole, RecognitionModel> roleModels;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public FaceRecognitionService() {
        try {
            this.faceDetector = new CascadeClassifier(HAAR_CASCADE_PATH);
            if (faceDetector.empty()) {
                logger.error("Failed to load cascade classifier from: {}", HAAR_CASCADE_PATH);
            }
            this.roleModels = new HashMap<>();
            initializeRoleModels();
            logger.info("FaceRecognitionService initialized with role-based models");
        } catch (Exception e) {
            logger.error("Error initializing FaceRecognitionService", e);
        }
    }

    /**
     * Initialize recognition models for each worker role
     */
    private void initializeRoleModels() {
        roleModels.put(WorkerRole.DRIVER, new RecognitionModel(
            WorkerRole.DRIVER,
            DRIVER_MATCH_THRESHOLD,
            "Driver Face Recognition Model (Optimized)"
        ));
        
        roleModels.put(WorkerRole.CLEANER, new RecognitionModel(
            WorkerRole.CLEANER,
            CLEANER_MATCH_THRESHOLD,
            "Cleaner Face Recognition Model"
        ));
        
        roleModels.put(WorkerRole.HELPER, new RecognitionModel(
            WorkerRole.HELPER,
            HELPER_MATCH_THRESHOLD,
            "Helper Face Recognition Model"
        ));
        
        roleModels.put(WorkerRole.SUPERVISOR, new RecognitionModel(
            WorkerRole.SUPERVISOR,
            SUPERVISOR_MATCH_THRESHOLD,
            "Supervisor Face Recognition Model"
        ));
        
        roleModels.put(WorkerRole.MANAGER, new RecognitionModel(
            WorkerRole.MANAGER,
            MANAGER_MATCH_THRESHOLD,
            "Manager Face Recognition Model"
        ));
        
        logger.info("Initialized {} role-based recognition models", roleModels.size());
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
     * Compare two face images and return similarity score (generic)
     *
     * @param face1Path Path to first face image
     * @param face2Path Path to second face image
     * @return Similarity score between 0 and 1
     */
    public double compareFaces(String face1Path, String face2Path) {
        return compareFacesByRole(face1Path, face2Path, null);
    }

    /**
     * Compare two face images with role-specific optimization
     *
     * @param face1Path Path to first face image (reference)
     * @param face2Path Path to second face image (captured)
     * @param workerRole The role of the worker (null for default threshold)
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

        face1.release();
        face2.release();
        face1Resized.release();
        face2Resized.release();
        face1Gray.release();
        face2Gray.release();
        hist1.release();
        hist2.release();

        if (workerRole != null) {
            logger.debug("Face similarity for role {}: {}", workerRole.getDisplayName(), similarity);
        }

        return similarity;
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
     * Check if face matches threshold for recognition (generic)
     *
     * @param similarity Similarity score
     * @return true if similarity meets threshold
     */
    public boolean isMatchConfident(double similarity) {
        return similarity >= MATCH_THRESHOLD;
    }

    /**
     * Check if face matches role-specific threshold
     *
     * @param similarity Similarity score
     * @param workerRole The role of the worker
     * @return true if similarity meets role-specific threshold
     */
    public boolean isMatchConfidentForRole(double similarity, WorkerRole workerRole) {
        double threshold = getThresholdForRole(workerRole);
        boolean match = similarity >= threshold;
        logger.debug("Role-based confidence check for {}: similarity={}, threshold={}, match={}",
                workerRole.getDisplayName(), String.format("%.4f", similarity),
                String.format("%.4f", threshold), match);
        return match;
    }

    /**
     * Get threshold for a specific worker role
     *
     * @param role Worker role
     * @return Threshold value (0-1)
     */
    public double getThresholdForRole(WorkerRole role) {
        if (role == null) return MATCH_THRESHOLD;
        
        return switch (role) {
            case DRIVER -> DRIVER_MATCH_THRESHOLD;
            case CLEANER -> CLEANER_MATCH_THRESHOLD;
            case HELPER -> HELPER_MATCH_THRESHOLD;
            case SUPERVISOR -> SUPERVISOR_MATCH_THRESHOLD;
            case MANAGER -> MANAGER_MATCH_THRESHOLD;
            default -> MATCH_THRESHOLD;
        };
    }

    /**
     * Capture frame from camera (requires camera input)
     *
     * @param deviceId Camera device ID (usually 0 for default camera)
     * @return Mat containing captured frame
     */
    public Mat captureCameraFrame(int deviceId) {
        VideoCapture camera = new VideoCapture(deviceId);
        if (!camera.isOpened()) {
            logger.error("Failed to open camera device: {}", deviceId);
            return new Mat();
        }

        Mat frame = new Mat();
        if (camera.read(frame)) {
            logger.info("Frame captured successfully");
        } else {
            logger.error("Failed to capture frame from camera");
        }

        camera.release();
        return frame;
    }

    /**
     * Verify multiple faces from a single image (generic)
     *
     * @param imagePath Path to image containing faces
     * @param workerFacePaths List of worker face reference paths
     * @return Matched worker index or -1 if no match
     */
    public int verifyWorkerFromImage(String imagePath, List<String> workerFacePaths) {
        FaceVerificationResult result = verifyWorkerFromImageByRole(imagePath, workerFacePaths, null);
        return result.isMatched() ? result.getMatchedIndex() : -1;
    }

    /**
     * Verify worker from captured image against multiple reference faces with role-specific threshold
     *
     * @param imagePath Path to captured image
     * @param referenceFacePaths List of reference face paths
     * @param workerRole The role of the worker (null for default threshold)
     * @return Recognition result with matched index and confidence
     */
    public FaceVerificationResult verifyWorkerFromImageByRole(String imagePath, 
                                                              List<String> referenceFacePaths, 
                                                              WorkerRole workerRole) {
        List<Rect> detectedFaces = detectFaces(imagePath);
        if (detectedFaces.isEmpty()) {
            logger.warn("No faces detected in image");
            return new FaceVerificationResult(-1, 0.0, getThresholdForRole(workerRole), false);
        }

        double bestMatch = 0.0;
        int bestMatchIndex = -1;
        double threshold = getThresholdForRole(workerRole);

        for (int i = 0; i < referenceFacePaths.size(); i++) {
            double similarity = compareFacesByRole(imagePath, referenceFacePaths.get(i), workerRole);
            if (similarity > bestMatch) {
                bestMatch = similarity;
                bestMatchIndex = i;
            }
        }

        boolean isMatched = bestMatch >= threshold;
        
        if (workerRole != null) {
            logger.info("Worker verification for role {}: bestMatch={}, threshold={}, matchedIndex={}, matched={}",
                    workerRole.getDisplayName(), String.format("%.4f", bestMatch),
                    String.format("%.4f", threshold), bestMatchIndex, isMatched);
        }

        return new FaceVerificationResult(bestMatchIndex, bestMatch, threshold, isMatched);
    }

    /**
     * Get recognition model for a specific role
     *
     * @param role Worker role
     * @return Recognition model for the role
     */
    public RecognitionModel getModelForRole(WorkerRole role) {
        return roleModels.get(role);
    }

    /**
     * Get all role models
     *
     * @return Map of all role models
     */
    public Map<WorkerRole, RecognitionModel> getAllModels() {
        return new HashMap<>(roleModels);
    }

    /**
     * Print statistics for all role models
     */
    public void printAllModelStatistics() {
        logger.info("===== Face Recognition Models Statistics =====");
        roleModels.forEach((role, model) -> {
            logger.info("{}: {}", role.getDisplayName(), model.getStatistics());
        });
        logger.info("==============================================");
    }

    /**
     * Inner class representing a face recognition model for a specific role
     */
    public static class RecognitionModel {
        private final WorkerRole role;
        private final double threshold;
        private final String modelName;
        private long trainedFaceCount;
        private long totalComparisons;
        private long successfulMatches;

        public RecognitionModel(WorkerRole role, double threshold, String modelName) {
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
                "%s | Threshold: %.2f | Trained: %d | Comparisons: %d | Successful: %d | Accuracy: %.2f%%",
                modelName, threshold, trainedFaceCount, totalComparisons, successfulMatches, getAccuracy()
            );
        }
    }

    /**
     * Result class for face verification
     */
    public static class FaceVerificationResult {
        private final int matchedIndex;
        private final double bestSimilarity;
        private final double threshold;
        private final boolean matched;

        public FaceVerificationResult(int matchedIndex, double bestSimilarity, double threshold, boolean matched) {
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
                "FaceVerificationResult{matchedIndex=%d, bestSimilarity=%.4f, threshold=%.4f, matched=%s}",
                matchedIndex, bestSimilarity, threshold, matched
            );
        }
    }
}
