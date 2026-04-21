# Face Recognition Architecture - Visual Guide

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                  Unified Face Recognition Service               │
│                     (Single Service Class)                       │
└─────────────────────────────────────────────────────────────────┘
                                 │
                 ┌───────────────┼───────────────┐
                 ▼               ▼               ▼
            ┌─────────┐      ┌─────────┐    ┌─────────┐
            │ Generic │      │  Role   │    │  Core   │
            │ Methods │      │Specific │    │Features │
            │(0.75)   │      │Methods  │    │         │
            └─────────┘      └─────────┘    └─────────┘
                │                  │            │
        • compareFaces()    • compareFacesByRole()
        • isMatchConfident()• isMatchConfidentForRole()  ← Uses role thresholds
        • verifyWorkerFrom  • verifyWorkerFromImageByRole()
          Image()           • getThresholdForRole()
                            • getModelForRole()
                            • printAllModelStatistics()
```

## Role-Based Models Structure

```
┌────────────────────────────────────────────────────────────────────┐
│                    Role-Based Models (5 Models)                     │
├────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐ │
│  │     DRIVER       │  │    CLEANER       │  │     HELPER       │ │
│  │  (OPTIMIZED) ⭐  │  │   (Standard)     │  │   (Standard)     │ │
│  │  Threshold: 0.80 │  │ Threshold: 0.75  │  │ Threshold: 0.75  │ │
│  │  Vehicle Access  │  │ Waste Collection │  │  Support Tasks   │ │
│  │  ↓Stricter       │  │  ↓Standard       │  │  ↓Standard       │ │
│  │  Security Level  │  │  Security Level  │  │  Security Level  │ │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘ │
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐                        │
│  │   SUPERVISOR     │  │     MANAGER      │                        │
│  │  (Stricter)      │  │  (Stricter)      │                        │
│  │  Threshold: 0.78 │  │ Threshold: 0.78  │                        │
│  │  Supervision     │  │  Management      │                        │
│  │  ↓Slightly +     │  │  ↓Slightly +     │                        │
│  │  Security Level  │  │  Security Level  │                        │
│  └──────────────────┘  └──────────────────┘                        │
│                                                                     │
└────────────────────────────────────────────────────────────────────┘
```

## Face Recognition Flow

### For Driver Check-In

```
┌─────────────────────────────────────────────────────────────────┐
│ DRIVER CHECK-IN PROCESS WITH OPTIMIZED RECOGNITION              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Capture Image
│     └─→ faceService.captureFrame() → Mat image
│
│  2. Detect Faces
│     └─→ faceService.detectFaces(imagePath) → List<Rect>
│
│  3. Compare with Reference (ROLE-SPECIFIC)
│     └─→ faceService.compareFacesByRole(
│              refFace, capFace, WorkerRole.DRIVER)
│         → similarity = 0.82 (example)
│
│  4. Validate with DRIVER Threshold (0.80)
│     └─→ faceService.isMatchConfidentForRole(0.82, DRIVER)
│         → true ✓  (0.82 >= 0.80)
│
│  5. Record Check-In
│     └─→ attendanceService.recordCheckIn(worker, ...)
│         → AttendanceRecord created with confidence: 0.82
│
└─────────────────────────────────────────────────────────────────┘
```

### For Worker (Cleaner) Check-In

```
┌─────────────────────────────────────────────────────────────────┐
│ WORKER CHECK-IN PROCESS WITH STANDARD RECOGNITION               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. Capture Image
│     └─→ faceService.captureFrame() → Mat image
│
│  2. Detect Faces
│     └─→ faceService.detectFaces(imagePath) → List<Rect>
│
│  3. Compare with Reference (ROLE-SPECIFIC)
│     └─→ faceService.compareFacesByRole(
│              refFace, capFace, WorkerRole.CLEANER)
│         → similarity = 0.76 (example)
│
│  4. Validate with CLEANER Threshold (0.75)
│     └─→ faceService.isMatchConfidentForRole(0.76, CLEANER)
│         → true ✓  (0.76 >= 0.75)
│
│  5. Record Check-In
│     └─→ attendanceService.recordCheckIn(worker, ...)
│         → AttendanceRecord created with confidence: 0.76
│
└─────────────────────────────────────────────────────────────────┘
```

## Decision Tree - Face Recognition

```
                        Start Face Recognition
                                 │
                                 ▼
                          Get Worker Role
                                 │
                    ┌────────────┼────────────┐
                    ▼            ▼            ▼
                  DRIVER      CLEANER      SUPERVISOR
                    │            │            │
            Threshold: 0.80  Threshold: 0.75 Threshold: 0.78
                    │            │            │
                    ▼            ▼            ▼
            Compare Faces with Role-Specific Model
                    │            │            │
                    ▼            ▼            ▼
            Similarity >= 0.80? Similarity >= 0.75? ...
                    │            │            │
            ┌───────┴───────┐    │            │
            ▼               ▼    │            │
          YES              NO    ▼            ▼
            │               │   YES          ...
     Check-In OK!   ✗ Reject │
                        │    └────────────────┐
                        │                     ▼
                        │              Check-In OK!
                        │
                        ▼
                  ✗ Attendance Failed
                  (Confidence Too Low)
```

## Threshold Comparison Example

```
Similarity Score: 0.77
│
├─ DRIVER (threshold 0.80)
│  └─→ 0.77 < 0.80 ✗ REJECTED (too strict)
│
├─ CLEANER (threshold 0.75)
│  └─→ 0.77 >= 0.75 ✓ ACCEPTED
│
├─ HELPER (threshold 0.75)
│  └─→ 0.77 >= 0.75 ✓ ACCEPTED
│
├─ SUPERVISOR (threshold 0.78)
│  └─→ 0.77 < 0.78 ✗ REJECTED (slightly too strict)
│
└─ MANAGER (threshold 0.78)
   └─→ 0.77 < 0.78 ✗ REJECTED (slightly too strict)

Note: Same face similarity score has different results
      based on worker role's security level!
```

## Multi-Face Recognition Flow

```
┌────────────────────────────────────────────────────────────────┐
│ VERIFYING WORKER FROM GROUP PHOTO (Multiple Faces)             │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  Input: Group photo with 5 drivers
│         Reference: [driver1.jpg, driver2.jpg, driver3.jpg,
│                     driver4.jpg, driver5.jpg]
│
│                      ▼
│              Detect All Faces
│                      │
│     ┌────────┬───────┼──────┬────────┐
│     ▼        ▼       ▼      ▼        ▼
│   Face 1   Face 2  Face 3 Face 4   Face 5
│     │        │       │      │        │
│     └────────┴───────┼──────┴────────┘
│                      ▼
│        Compare Against All References
│        (Using DRIVER threshold: 0.80)
│                      │
│     ┌────────┬───────┼──────┬────────┐
│     ▼        ▼       ▼      ▼        ▼
│   0.65      0.78    0.82   0.71     0.88
│   (low)     (low)  (match)  (low)   (BEST!)
│                      │       │
│                      ▼       ▼
│                   Best Match: Face 5
│                   Similarity: 0.88
│                   Threshold: 0.80
│                   Status: ✓ MATCHED (Driver #5)
│
└────────────────────────────────────────────────────────────────┘
```

## RecognitionModel Statistics

```
┌─────────────────────────────────────────────────────────────┐
│ RECOGNITION MODEL FOR DRIVER ROLE                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Model Name: Driver Face Recognition Model (Optimized)    │
│  Role: DRIVER                                              │
│  Threshold: 0.80                                           │
│                                                             │
│  ╔════════════════════════════════════════════════════╗   │
│  ║ Performance Metrics                                ║   │
│  ╠════════════════════════════════════════════════════╣   │
│  ║ Trained Face Count: 150                            ║   │
│  ║ Total Comparisons: 1245                            ║   │
│  ║ Successful Matches: 1189                           ║   │
│  ║ Accuracy: 95.51%                                   ║   │
│  ║ Accuracy Rate: ████████████████████░ 95.51%        ║   │
│  ╚════════════════════════════════════════════════════╝   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Integration with Attendance System

```
┌──────────────────────────────────────────────────────────────────┐
│                    FULL ATTENDANCE FLOW                           │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ 1. Worker Approaches Camera                             │   │
│  └──────────────────┬──────────────────────────────────────┘   │
│                     │                                            │
│  ┌──────────────────▼──────────────────────────────────────┐   │
│  │ 2. Capture Image (from camera or file)                 │   │
│  └──────────────────┬──────────────────────────────────────┘   │
│                     │                                            │
│  ┌──────────────────▼──────────────────────────────────────┐   │
│  │ 3. FaceRecognitionService.compareFacesByRole()          │   │
│  │    ├─ Loads Reference Face (stored during registration)│   │
│  │    ├─ Loads Captured Face                              │   │
│  │    ├─ Compares using role-specific model               │   │
│  │    └─ Returns similarity score                         │   │
│  └──────────────────┬──────────────────────────────────────┘   │
│                     │                                            │
│  ┌──────────────────▼──────────────────────────────────────┐   │
│  │ 4. Validate with Role Threshold                         │   │
│  │    isMatchConfidentForRole(similarity, role)            │   │
│  └──────────────────┬──────────────────────────────────────┘   │
│                     │                                            │
│          ┌──────────┴──────────┐                                 │
│          ▼                     ▼                                 │
│       PASS                   FAIL                                │
│          │                     │                                 │
│  ┌───────▼──────────┐  ┌───────▼──────────┐                    │
│  │ 5a. Create       │  │ 5b. Reject       │                    │
│  │     Attendance   │  │     Check-In     │                    │
│  │     Record       │  │     (Low Face    │                    │
│  │     with GPS     │  │      Confidence) │                    │
│  └───────┬──────────┘  └───────┬──────────┘                    │
│          │                     │                                 │
│  ┌───────▼──────────┐  ┌───────▼──────────┐                    │
│  │ 6a. Mark as      │  │ 6b. Log Failed   │                    │
│  │     PRESENT      │  │     Attempt      │                    │
│  │     or LATE      │  │     (Security)   │                    │
│  └──────────────────┘  └──────────────────┘                    │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

## Method Call Sequence

```
FaceRecognitionService Service Initialization
        │
        ├─→ Initialize Cascade Classifier
        ├─→ Create RecognitionModel for DRIVER (0.80)
        ├─→ Create RecognitionModel for CLEANER (0.75)
        ├─→ Create RecognitionModel for HELPER (0.75)
        ├─→ Create RecognitionModel for SUPERVISOR (0.78)
        └─→ Create RecognitionModel for MANAGER (0.78)

During Check-In
        │
        ├─→ detectFaces(imagePath)
        │   └─→ Returns List<Rect> of faces
        │
        ├─→ compareFacesByRole(ref, cap, WorkerRole.DRIVER)
        │   ├─→ Load images
        │   ├─→ Resize to 224x224
        │   ├─→ Convert to grayscale
        │   ├─→ Compute histograms
        │   ├─→ Compare histograms
        │   └─→ Returns similarity (0-1)
        │
        ├─→ isMatchConfidentForRole(0.82, WorkerRole.DRIVER)
        │   ├─→ Get threshold: 0.80
        │   ├─→ Check: 0.82 >= 0.80 ?
        │   └─→ Returns: true/false
        │
        └─→ Create AttendanceRecord
            ├─→ Set worker
            ├─→ Set check-in time
            ├─→ Set location (GPS)
            ├─→ Set face confidence
            └─→ Save to database
```

---

This visual guide shows:
- ✅ Service architecture
- ✅ Role-based models
- ✅ Recognition flow
- ✅ Decision trees
- ✅ Threshold comparison
- ✅ Integration points
- ✅ Method sequences
