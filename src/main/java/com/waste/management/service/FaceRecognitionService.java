package com.waste.management.service;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Face Recognition Service using OpenCV for detecting and matching faces
 */
public class FaceRecognitionService {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionService.class);
    private static final double MATCH_THRESHOLD = 0.75;
    private static final String HAAR_CASCADE_PATH = "haarcascade_frontalface_alt.xml";

    private CascadeClassifier faceDetector;

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public FaceRecognitionService() {
        try {
            this.faceDetector = new CascadeClassifier(HAAR_CASCADE_PATH);
            if (faceDetector.empty()) {
                logger.error("Failed to load cascade classifier");
            }
        } catch (Exception e) {
            logger.error("Error initializing FaceRecognitionService", e);
        }
    }

    /**
     * Detect faces in an image
     *
     * @param imagePath Path to the image file
     * @return List of detected faces as MatOfRect
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
     * Compare two face images and return similarity score
     *
     * @param face1Path Path to first face image
     * @param face2Path Path to second face image
     * @return Similarity score between 0 and 1
     */
    public double compareFaces(String face1Path, String face2Path) {
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
     * Check if face matches threshold for recognition
     *
     * @param similarity Similarity score
     * @return true if similarity meets threshold
     */
    public boolean isMatchConfident(double similarity) {
        return similarity >= MATCH_THRESHOLD;
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
     * Verify multiple faces from a single image
     *
     * @param imagePath Path to image containing faces
     * @param workerFacePaths List of worker face reference paths
     * @return Matched worker index or -1 if no match
     */
    public int verifyWorkerFromImage(String imagePath, List<String> workerFacePaths) {
        List<Rect> detectedFaces = detectFaces(imagePath);
        if (detectedFaces.isEmpty()) {
            logger.warn("No faces detected in image");
            return -1;
        }

        // Use first detected face for verification
        double bestMatch = 0.0;
        int bestMatchIndex = -1;

        for (int i = 0; i < workerFacePaths.size(); i++) {
            double similarity = compareFaces(imagePath, workerFacePaths.get(i));
            if (similarity > bestMatch) {
                bestMatch = similarity;
                bestMatchIndex = i;
            }
        }

        if (isMatchConfident(bestMatch)) {
            return bestMatchIndex;
        }

        return -1;
    }
}
