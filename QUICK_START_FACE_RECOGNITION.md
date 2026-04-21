# Quick Reference - Face Recognition for Drivers & Workers

## 🎯 At a Glance

**Single unified service** with **role-specific optimization** for both **Drivers** and **Workers**.

---

## 📦 What You Get

### Service: `FaceRecognitionService`
- ✅ Generic methods (backward compatible)
- ✅ Role-specific methods (new)
- ✅ Performance tracking per role
- ✅ Multiple recognition models

### Models Available
| Role | Threshold | Use Case |
|------|-----------|----------|
| **DRIVER** | 0.80 | 🚗 Vehicle operation (stricter) |
| **CLEANER** | 0.75 | 🧹 Waste collection (standard) |
| **HELPER** | 0.75 | 🤝 Support role (standard) |
| **SUPERVISOR** | 0.78 | 👔 Supervision (stricter) |
| **MANAGER** | 0.78 | 📋 Management (stricter) |

---

## 💻 Quick Code Examples

### Initialize Service
```java
FaceRecognitionService faceService = new FaceRecognitionService();
```

### Verify a Driver (Stricter: 0.80)
```java
double driverSimilarity = faceService.compareFacesByRole(
    "ref/driver.jpg", "cap/check_in.jpg", WorkerRole.DRIVER
);
boolean match = faceService.isMatchConfidentForRole(driverSimilarity, WorkerRole.DRIVER);
```

### Verify a Worker (Standard: 0.75)
```java
double workerSimilarity = faceService.compareFacesByRole(
    "ref/cleaner.jpg", "cap/check_in.jpg", WorkerRole.CLEANER
);
boolean match = faceService.isMatchConfidentForRole(workerSimilarity, WorkerRole.CLEANER);
```

### Multi-Face Verification (E.g., Drivers)
```java
List<String> driverReferences = Arrays.asList(
    "drivers/driver1.jpg", "drivers/driver2.jpg", "drivers/driver3.jpg"
);

FaceRecognitionService.FaceVerificationResult result = 
    faceService.verifyWorkerFromImageByRole(
        "cap/group_photo.jpg", driverReferences, WorkerRole.DRIVER
    );

if (result.isMatched()) {
    System.out.println("Driver matched at index: " + result.getMatchedIndex());
}
```

### Get Threshold for Role
```java
double threshold = faceService.getThresholdForRole(WorkerRole.DRIVER);
// Returns: 0.80
```

### Get Model Info
```java
FaceRecognitionService.RecognitionModel model = 
    faceService.getModelForRole(WorkerRole.DRIVER);
System.out.println(model.getStatistics());
// Output: Driver Face Recognition Model | Threshold: 0.80 | Trained: 150 | Comparisons: 1245 | Successful: 1189 | Accuracy: 95.51%
```

### Print All Statistics
```java
faceService.printAllModelStatistics();
// Shows stats for all 5 role models
```

---

## 🔄 Integration Example

### In AttendanceService
```java
public Optional<AttendanceRecord> recordCheckIn(Worker worker, String imagePath, 
                                                Double latitude, Double longitude) {
    // Use role-specific recognition
    double similarity = faceService.compareFacesByRole(
        imagePath, 
        worker.getFacialDataPath(),
        worker.getRole()  // ← DRIVER or CLEANER etc.
    );
    
    // Validate with role-specific threshold
    if (!faceService.isMatchConfidentForRole(similarity, worker.getRole())) {
        logger.warn("Face verification failed for {}", worker.getRole());
        return Optional.empty();
    }
    
    // Create attendance record
    AttendanceRecord record = new AttendanceRecord();
    record.setWorker(worker);
    record.setFaceMatchConfidence(similarity);
    // ... rest of logic
}
```

---

## 🔧 All Available Methods

### Generic (Default Threshold: 0.75)
- `compareFaces(face1, face2)` → double
- `isMatchConfident(similarity)` → boolean
- `verifyWorkerFromImage(image, references)` → int

### Role-Specific (Custom Thresholds)
- `compareFacesByRole(face1, face2, role)` → double
- `isMatchConfidentForRole(similarity, role)` → boolean
- `verifyWorkerFromImageByRole(image, references, role)` → FaceVerificationResult
- `getThresholdForRole(role)` → double
- `getModelForRole(role)` → RecognitionModel
- `printAllModelStatistics()` → void

### Core Features
- `detectFaces(imagePath)` → List<Rect>
- `extractFaceRegion(imagePath, region)` → Mat
- `captureCameraFrame(deviceId)` → Mat

---

## 📊 Result Objects

### FaceVerificationResult
```java
result.getMatchedIndex()      // Index of matched face (-1 if no match)
result.getBestSimilarity()    // Highest similarity score (0-1)
result.getThreshold()         // Role-specific threshold used
result.isMatched()            // true if matched, false otherwise
```

### RecognitionModel
```java
model.getRole()               // WorkerRole enum
model.getThreshold()          // Threshold for this role
model.getModelName()          // Display name
model.getTrainedFaceCount()   // Number of trained faces
model.getTotalComparisons()   // Total face comparisons done
model.getSuccessfulMatches()  // Number of successful matches
model.getAccuracy()           // Accuracy percentage
```

---

## 🎓 Why Different Thresholds?

| Reason | Driver (0.80) | Worker (0.75) |
|--------|---------------|---------------|
| **Risk Level** | High (vehicle access) | Standard (tools only) |
| **Security** | Stricter verification | Standard verification |
| **Accuracy** | Requires 80%+ confidence | Requires 75%+ confidence |
| **False Positives** | Less tolerated | More acceptable |

---

## ✅ Backward Compatibility

Old code still works:
```java
// This still works with default threshold (0.75)
double sim = faceService.compareFaces(face1, face2);
boolean match = faceService.isMatchConfident(sim);
```

---

## 📁 Files to Check Out

1. **`FACE_RECOGNITION_GUIDE.md`** - Detailed usage guide
2. **`FaceRecognitionExample.java`** - Runnable examples
3. **`IMPLEMENTATION_SUMMARY.md`** - Full technical summary

---

## 🚀 Getting Started

1. Create `FaceRecognitionService` instance
2. Call role-specific methods with worker's role
3. Get verification results
4. Check model statistics

That's it! 🎉

---

## 💡 Pro Tips

- **Driver verification**: Always use `WorkerRole.DRIVER` for stricter matching
- **Monitor accuracy**: Call `printAllModelStatistics()` periodically
- **Optimize performance**: Cache frequently compared faces
- **Security**: Implement rate limiting on verification attempts
- **Logging**: Enable debug logs to see verification details

---

## ❓ FAQ

**Q: Can I still use the old generic methods?**  
A: Yes! Full backward compatibility maintained.

**Q: How do I change thresholds?**  
A: Modify constants in FaceRecognitionService (e.g., `DRIVER_MATCH_THRESHOLD`)

**Q: Which threshold should I use for a new role?**  
A: Start with 0.75 (default), increase for higher security

**Q: How do I track model performance?**  
A: Call `getModelForRole()` or `printAllModelStatistics()`

---

## 📞 Need Help?

Check these files:
- **Usage examples**: `FaceRecognitionExample.java`
- **Detailed guide**: `FACE_RECOGNITION_GUIDE.md`
- **Full API**: `FaceRecognitionService.java` source code
- **Integration help**: `IMPLEMENTATION_SUMMARY.md`
