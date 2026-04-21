# Unified Face Recognition Service - Driver & Worker Models

## Overview

The enhanced `FaceRecognitionService` now provides **unified face recognition** with **role-specific optimization** for both **Drivers** and **Workers** (and other roles). All functionality is consolidated in a single service with separate recognition models for each worker role.

## Features

### 🎯 Role-Based Recognition Models

The service automatically initializes dedicated recognition models for each worker role:

| Role | Threshold | Description |
|------|-----------|-------------|
| **DRIVER** | 0.80 | Stricter threshold - Optimized for driver verification |
| **CLEANER** | 0.75 | Standard threshold |
| **HELPER** | 0.75 | Standard threshold |
| **SUPERVISOR** | 0.78 | Slightly stricter threshold |
| **MANAGER** | 0.78 | Slightly stricter threshold |

### 🔍 Key Capabilities

1. **Generic Face Recognition** - Works with default threshold
2. **Role-Specific Recognition** - Uses role-specific thresholds for accuracy
3. **Face Detection** - Detect multiple faces in images
4. **Feature Extraction** - Extract facial features from detected regions
5. **Histogram-Based Comparison** - Compare faces using histogram analysis
6. **Model Statistics** - Track performance metrics per role
7. **Camera Integration** - Capture frames directly from camera

## Usage Examples

### 1. Basic Initialization

```java
// Initialize the service (automatically creates all role models)
FaceRecognitionService faceService = new FaceRecognitionService();
```

### 2. Generic Face Comparison (Default Threshold)

```java
// Compare two face images using default threshold (0.75)
double similarity = faceService.compareFaces("path/to/reference.jpg", "path/to/captured.jpg");

// Check if faces match
boolean isMatch = faceService.isMatchConfident(similarity);
System.out.println("Face match: " + isMatch + " (similarity: " + similarity + ")");
```

### 3. Driver Face Recognition (Stricter Threshold)

```java
// Compare faces using DRIVER-specific threshold (0.80)
double driverSimilarity = faceService.compareFacesByRole(
    "data/faces/driver_john.jpg",
    "data/faces/captured_image.jpg",
    WorkerRole.DRIVER
);

// Check with driver-specific threshold
boolean isDriverMatch = faceService.isMatchConfidentForRole(driverSimilarity, WorkerRole.DRIVER);
System.out.println("Driver face match: " + isDriverMatch);
```

### 4. Worker Face Recognition

```java
// Compare faces using WORKER/CLEANER-specific threshold (0.75)
double workerSimilarity = faceService.compareFacesByRole(
    "data/faces/cleaner_mike.jpg",
    "data/faces/captured_image.jpg",
    WorkerRole.CLEANER
);

boolean isWorkerMatch = faceService.isMatchConfidentForRole(workerSimilarity, WorkerRole.CLEANER);
System.out.println("Worker face match: " + isWorkerMatch);
```

### 5. Verify Worker from Multiple Reference Faces (Role-Specific)

```java
// List of reference face paths for multiple drivers
List<String> driverReferenceFaces = Arrays.asList(
    "data/faces/drivers/driver_john.jpg",
    "data/faces/drivers/driver_sarah.jpg",
    "data/faces/drivers/driver_mike.jpg"
);

// Verify captured image against driver reference faces
FaceRecognitionService.FaceVerificationResult driverResult = 
    faceService.verifyWorkerFromImageByRole(
        "data/captured/check_in_image.jpg",
        driverReferenceFaces,
        WorkerRole.DRIVER
    );

if (driverResult.isMatched()) {
    System.out.println("Driver matched at index: " + driverResult.getMatchedIndex());
    System.out.println("Similarity: " + driverResult.getBestSimilarity());
} else {
    System.out.println("No matching driver found");
}
```

### 6. Verify Multiple Worker Types

```java
// List of cleaner reference faces
List<String> cleanerReferenceFaces = Arrays.asList(
    "data/faces/cleaners/cleaner_alice.jpg",
    "data/faces/cleaners/cleaner_bob.jpg"
);

// Verify with CLEANER role
FaceRecognitionService.FaceVerificationResult cleanerResult = 
    faceService.verifyWorkerFromImageByRole(
        "data/captured/check_in_image.jpg",
        cleanerReferenceFaces,
        WorkerRole.CLEANER
    );

System.out.println("Cleaner verification: " + cleanerResult);
```

### 7. Get Recognition Model for a Role

```java
// Get the DRIVER recognition model
FaceRecognitionService.RecognitionModel driverModel = faceService.getModelForRole(WorkerRole.DRIVER);

System.out.println("Model Name: " + driverModel.getModelName());
System.out.println("Threshold: " + driverModel.getThreshold());
System.out.println("Trained Faces: " + driverModel.getTrainedFaceCount());
System.out.println("Total Comparisons: " + driverModel.getTotalComparisons());
System.out.println("Accuracy: " + driverModel.getAccuracy() + "%");
```

### 8. Print All Model Statistics

```java
// Print statistics for all role models
faceService.printAllModelStatistics();

// Output:
// ===== Face Recognition Models Statistics =====
// Driver: Driver Face Recognition Model | Threshold: 0.80 | Trained: 150 | Comparisons: 1245 | Successful: 1189 | Accuracy: 95.51%
// Cleaner: Cleaner Face Recognition Model | Threshold: 0.75 | Trained: 200 | Comparisons: 1562 | Successful: 1421 | Accuracy: 91.03%
// ... etc
// ==============================================
```

### 9. Detect Faces in Image

```java
// Detect all faces in an image
List<Rect> faces = faceService.detectFaces("path/to/image_with_faces.jpg");

System.out.println("Detected " + faces.size() + " faces");
for (int i = 0; i < faces.size(); i++) {
    Rect face = faces.get(i);
    System.out.println("Face " + i + ": x=" + face.x + ", y=" + face.y + 
                      ", width=" + face.width + ", height=" + face.height);
}
```

### 10. Extract Face Region

```java
// Detect faces first
List<Rect> detectedFaces = faceService.detectFaces("path/to/image.jpg");

if (!detectedFaces.isEmpty()) {
    // Extract the first face region
    Mat firstFaceRegion = faceService.extractFaceRegion("path/to/image.jpg", detectedFaces.get(0));
    
    // Use the extracted face for further processing
    System.out.println("Extracted face region with size: " + firstFaceRegion.size());
}
```

### 11. Get Threshold for a Role

```java
// Get the threshold for a specific role
double driverThreshold = faceService.getThresholdForRole(WorkerRole.DRIVER);
double cleanerThreshold = faceService.getThresholdForRole(WorkerRole.CLEANER);

System.out.println("Driver threshold: " + driverThreshold);
System.out.println("Cleaner threshold: " + cleanerThreshold);
```

### 12. Capture from Camera

```java
// Capture frame from default camera (device 0)
Mat cameraFrame = faceService.captureCameraFrame(0);

if (!cameraFrame.empty()) {
    // Save or process the captured frame
    System.out.println("Frame captured: " + cameraFrame.size());
}
```

## Integration with Attendance Service

```java
// Enhanced AttendanceService using role-based recognition
public Optional<AttendanceRecord> recordCheckInWithRoleOptimization(
    Worker worker, String imagePath, Double latitude, Double longitude) {
    
    try {
        // Use role-specific face recognition
        double faceConfidence = faceService.compareFacesByRole(
            imagePath, 
            worker.getFacialDataPath(),
            worker.getRole()  // DRIVER or other roles
        );
        
        // Use role-specific threshold
        if (!faceService.isMatchConfidentForRole(faceConfidence, worker.getRole())) {
            logger.warn("Face verification failed for {}: {}", 
                worker.getRole().getDisplayName(), worker.getEmployeeId());
            return Optional.empty();
        }

        // Create attendance record
        AttendanceRecord record = new AttendanceRecord();
        record.setWorker(worker);
        record.setCheckInTime(LocalDateTime.now());
        record.setLocationLatitude(latitude);
        record.setLocationLongitude(longitude);
        record.setImageCapturedPath(imagePath);
        record.setFaceMatchConfidence(faceConfidence);
        record.setStatus(AttendanceStatus.PRESENT);

        return Optional.of(attendanceRepository.save(record));
    } catch (Exception e) {
        logger.error("Error recording check-in for worker: {}", worker.getEmployeeId(), e);
        return Optional.empty();
    }
}
```

## Threshold Comparison

### Why Different Thresholds?

- **DRIVER (0.80)**: Stricter threshold because:
  - Drivers handle vehicles and may have access to sensitive areas
  - Requires higher confidence for security
  - Accidents or unauthorized access by wrong driver is critical

- **CLEANER (0.75)**: Standard threshold because:
  - General waste management role
  - Standard security level appropriate

- **HELPER (0.75)**: Standard threshold

- **SUPERVISOR/MANAGER (0.78)**: Slightly stricter:
  - Leadership roles require better verification
  - More responsible for operations and safety

## Recognition Model Inner Class

```java
public static class RecognitionModel {
    private final WorkerRole role;
    private final double threshold;
    private final String modelName;
    private long trainedFaceCount;
    private long totalComparisons;
    private long successfulMatches;
    
    // Methods:
    // - getRole()
    // - getThreshold()
    // - getModelName()
    // - getTrainedFaceCount()
    // - getTotalComparisons()
    // - getSuccessfulMatches()
    // - incrementTrainedFaceCount()
    // - incrementTotalComparisons()
    // - incrementSuccessfulMatches()
    // - getAccuracy()
    // - getStatistics()
}
```

## FaceVerificationResult Inner Class

```java
public static class FaceVerificationResult {
    private final int matchedIndex;
    private final double bestSimilarity;
    private final double threshold;
    private final boolean matched;
    
    // Getters:
    // - getMatchedIndex()
    // - getBestSimilarity()
    // - getThreshold()
    // - isMatched()
}
```

## Performance Considerations

1. **Image Size**: Optimal performance with images 400x400 pixels or larger
2. **Lighting**: Better results with good lighting conditions
3. **Face Position**: Best results when face is frontal and centered
4. **Threshold Tuning**: Adjust thresholds based on your use case
5. **Caching**: Consider caching frequently compared faces for performance

## Security Best Practices

1. **Always use role-specific thresholds** for different worker types
2. **Store facial data securely** with proper permissions
3. **Log all verification attempts** for audit trails
4. **Validate images** before processing
5. **Implement rate limiting** for verification attempts
6. **Use HTTPS** for transmitting sensitive data

## Troubleshooting

### Low Recognition Accuracy
- Ensure good lighting conditions
- Verify reference face image quality
- Check face is frontal to camera
- Consider adjusting threshold values

### No Faces Detected
- Verify image path is correct
- Ensure image contains visible faces
- Try with better quality images
- Check lighting and contrast

### Performance Issues
- Reduce image size (pre-process)
- Use more powerful hardware
- Consider batching multiple comparisons
- Implement caching for reference faces

## Future Enhancements

- [ ] Deep learning-based face recognition (DL4J integration)
- [ ] Multi-face identification in single image
- [ ] Real-time video stream processing
- [ ] Liveness detection to prevent spoofing
- [ ] Age and gender estimation
- [ ] Emotion detection for driver fatigue
