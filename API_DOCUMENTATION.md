# Face Recognition with Worker Details Fetching - API Documentation

## Overview

This API provides face recognition capabilities combined with automatic worker details fetching. When a face is recognized, the system automatically retrieves complete worker information including name, phone, Aadhar, gender, and caste.

## Base URL

```
http://localhost:8080/api/recognition
```

## Authentication

Currently, no authentication is required. (To be implemented with JWT tokens in production)

---

## Endpoints

### 1. Recognize Driver and Fetch Details

Recognizes a driver face and automatically fetches complete driver details.

**Endpoint:** `POST /api/recognition/driver/details`

**Request:**
```
Content-Type: multipart/form-data

Parameter: image (MultipartFile) - The captured image file containing the driver's face
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Driver recognized successfully",
  "data": {
    "workerId": 1,
    "employeeId": "DRV001",
    "fullName": "John Doe",
    "phoneNumber": "+91-9876543210",
    "email": "john.doe@example.com",
    "role": "DRIVER",
    "aadharNumber": "1234-5678-9012",
    "gender": "MALE",
    "caste": "General",
    "department": "Transportation",
    "active": true,
    "faceMatchConfidence": 92.45,
    "message": "Driver recognized with high confidence"
  },
  "errorCode": null
}
```

**Error Response (404):**
```json
{
  "success": false,
  "message": "Driver not recognized. Face does not match any registered driver.",
  "data": null,
  "errorCode": null
}
```

**cURL Example:**
```bash
curl -X POST \
  http://localhost:8080/api/recognition/driver/details \
  -H 'Content-Type: multipart/form-data' \
  -F 'image=@path/to/driver/image.jpg'
```

---

### 2. Recognize Worker by Role and Fetch Details

Recognizes a worker face for a specific role and fetches their details.

**Endpoint:** `POST /api/recognition/worker/details`

**Request:**
```
Content-Type: multipart/form-data

Parameters:
  - image (MultipartFile) - The captured image file
  - role (String) - Worker role: DRIVER, CLEANER, HELPER, SUPERVISOR, MANAGER
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Worker recognized successfully",
  "data": {
    "workerId": 5,
    "employeeId": "CLN002",
    "fullName": "Maria Garcia",
    "phoneNumber": "+91-9876543211",
    "email": "maria.garcia@example.com",
    "role": "CLEANER",
    "aadharNumber": "5678-9012-3456",
    "gender": "FEMALE",
    "caste": "OBC",
    "department": "Sanitation",
    "active": true,
    "faceMatchConfidence": 87.33,
    "message": "Worker recognized successfully"
  },
  "errorCode": null
}
```

**Error Response (400):**
```json
{
  "success": false,
  "message": "Invalid role: INVALID_ROLE. Valid roles: DRIVER, CLEANER, HELPER, SUPERVISOR, MANAGER",
  "data": null,
  "errorCode": null
}
```

**cURL Example:**
```bash
curl -X POST \
  'http://localhost:8080/api/recognition/worker/details?role=CLEANER' \
  -H 'Content-Type: multipart/form-data' \
  -F 'image=@path/to/worker/image.jpg'
```

---

### 3. Auto-Detect Role and Fetch Details

Automatically detects the worker role by testing against all registered workers across all roles.

**Endpoint:** `POST /api/recognition/auto/details`

**Request:**
```
Content-Type: multipart/form-data

Parameter: image (MultipartFile) - The captured image file
```

**Recognition Priority:**
1. DRIVER (threshold: 0.80) - Strictest
2. SUPERVISOR (threshold: 0.78)
3. MANAGER (threshold: 0.78)
4. CLEANER (threshold: 0.75)
5. HELPER (threshold: 0.75)

**Success Response (200):**
```json
{
  "success": true,
  "message": "Worker recognized successfully",
  "data": {
    "workerId": 3,
    "employeeId": "SUP001",
    "fullName": "Ahmed Khan",
    "phoneNumber": "+91-9876543212",
    "email": "ahmed.khan@example.com",
    "role": "SUPERVISOR",
    "aadharNumber": "9012-3456-7890",
    "gender": "MALE",
    "caste": "General",
    "department": "Management",
    "active": true,
    "faceMatchConfidence": 89.78,
    "message": "Worker recognized as SUPERVISOR"
  },
  "errorCode": null
}
```

**cURL Example:**
```bash
curl -X POST \
  http://localhost:8080/api/recognition/auto/details \
  -H 'Content-Type: multipart/form-data' \
  -F 'image=@path/to/worker/image.jpg'
```

---

### 4. Get Worker Details by ID

Fetches worker details directly by worker ID (without face recognition).

**Endpoint:** `GET /api/recognition/worker/{id}`

**URL Parameters:**
- `id` (Long) - Worker ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Worker details retrieved successfully",
  "data": {
    "workerId": 1,
    "employeeId": "DRV001",
    "fullName": "John Doe",
    "phoneNumber": "+91-9876543210",
    "email": "john.doe@example.com",
    "role": "DRIVER",
    "aadharNumber": "1234-5678-9012",
    "gender": "MALE",
    "caste": "General",
    "department": "Transportation",
    "active": true,
    "faceMatchConfidence": 0.0,
    "message": "Details fetched without recognition"
  },
  "errorCode": null
}
```

**cURL Example:**
```bash
curl -X GET \
  http://localhost:8080/api/recognition/worker/1
```

---

### 5. Get Worker Details by Employee ID

Fetches worker details by employee ID (without face recognition).

**Endpoint:** `GET /api/recognition/worker/employee/{employeeId}`

**URL Parameters:**
- `employeeId` (String) - Employee ID

**Success Response (200):**
```json
{
  "success": true,
  "message": "Worker details retrieved successfully",
  "data": {
    "workerId": 2,
    "employeeId": "DRV002",
    "fullName": "Jane Smith",
    "phoneNumber": "+91-9876543213",
    "email": "jane.smith@example.com",
    "role": "DRIVER",
    "aadharNumber": "2345-6789-0123",
    "gender": "FEMALE",
    "caste": "SC",
    "department": "Transportation",
    "active": true,
    "faceMatchConfidence": 0.0,
    "message": "Details fetched without recognition"
  },
  "errorCode": null
}
```

**cURL Example:**
```bash
curl -X GET \
  http://localhost:8080/api/recognition/worker/employee/DRV002
```

---

### 6. Get All Driver Details

Fetches details of all registered drivers in the system.

**Endpoint:** `GET /api/recognition/driver/all`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Retrieved 3 driver(s)",
  "data": [
    {
      "workerId": 1,
      "employeeId": "DRV001",
      "fullName": "John Doe",
      "phoneNumber": "+91-9876543210",
      "email": "john.doe@example.com",
      "role": "DRIVER",
      "aadharNumber": "1234-5678-9012",
      "gender": "MALE",
      "caste": "General",
      "department": "Transportation",
      "active": true,
      "faceMatchConfidence": 0.0,
      "message": "All drivers retrieved"
    },
    {
      "workerId": 2,
      "employeeId": "DRV002",
      "fullName": "Jane Smith",
      "phoneNumber": "+91-9876543213",
      "email": "jane.smith@example.com",
      "role": "DRIVER",
      "aadharNumber": "2345-6789-0123",
      "gender": "FEMALE",
      "caste": "SC",
      "department": "Transportation",
      "active": true,
      "faceMatchConfidence": 0.0,
      "message": "All drivers retrieved"
    }
  ],
  "errorCode": null
}
```

**cURL Example:**
```bash
curl -X GET \
  http://localhost:8080/api/recognition/driver/all
```

---

### 7. Health Check

Verifies that the Face Recognition Service is running.

**Endpoint:** `GET /api/recognition/health`

**Success Response (200):**
```json
{
  "success": true,
  "message": "Face Recognition Service is running",
  "data": "OK",
  "errorCode": null
}
```

**cURL Example:**
```bash
curl -X GET \
  http://localhost:8080/api/recognition/health
```

---

## Response Codes

| Status Code | Description |
|------------|-------------|
| 200 | Success - Worker recognized or details fetched successfully |
| 400 | Bad Request - Invalid role or empty image file |
| 404 | Not Found - No matching worker or driver found |
| 500 | Internal Server Error - Server-side error during processing |

---

## WorkerDetailsDto Fields

| Field | Type | Description |
|-------|------|-------------|
| workerId | Long | Unique worker identifier |
| employeeId | String | Employee ID |
| fullName | String | Full name of the worker |
| phoneNumber | String | Contact phone number |
| email | String | Email address |
| role | WorkerRole | Worker role (DRIVER, CLEANER, HELPER, SUPERVISOR, MANAGER) |
| aadharNumber | String | Unique Aadhar number |
| gender | Gender | Gender (MALE, FEMALE, OTHER) |
| caste | String | Caste information |
| department | String | Department/Division |
| active | boolean | Whether worker is active |
| faceMatchConfidence | double | Face match confidence percentage (0-100) |
| message | String | Additional message or status |

---

## Error Handling

All errors return a consistent format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "errorCode": "ERROR_CODE"
}
```

---

## Usage Scenarios

### Scenario 1: Driver Check-in at Start of Shift
1. Capture driver's photo
2. Call `POST /api/recognition/driver/details`
3. If successful, display driver information for confirmation
4. Log attendance automatically

**Curl:**
```bash
curl -X POST \
  http://localhost:8080/api/recognition/driver/details \
  -F 'image=@driver_checkin.jpg'
```

### Scenario 2: General Worker Recognition (Any Role)
1. Capture worker photo
2. Call `POST /api/recognition/auto/details`
3. System auto-detects role and returns details
4. Update attendance records

**Curl:**
```bash
curl -X POST \
  http://localhost:8080/api/recognition/auto/details \
  -F 'image=@worker_photo.jpg'
```

### Scenario 3: Display All Drivers
1. Call `GET /api/recognition/driver/all`
2. Display list in UI with all driver details
3. Allow filtering/searching

**Curl:**
```bash
curl -X GET \
  http://localhost:8080/api/recognition/driver/all
```

---

## Face Recognition Thresholds

| Role | Threshold | Sensitivity |
|------|-----------|-------------|
| DRIVER | 0.80 | High (Strictest) |
| SUPERVISOR | 0.78 | High |
| MANAGER | 0.78 | High |
| CLEANER | 0.75 | Medium |
| HELPER | 0.75 | Medium |

**Note:** A face match must exceed the threshold for the worker's role to be recognized.

---

## Best Practices

1. **Image Quality**: Ensure images are clear, well-lit, and have the worker's face prominently visible
2. **Image Format**: Support JPG, PNG, and other common image formats
3. **Image Size**: Keep images under 5MB for optimal performance
4. **Error Handling**: Always check the `success` field in responses
5. **Confidence Levels**: Consider responses with confidence < 80% as lower certainty
6. **Caching**: Cache driver lists if fetched frequently to reduce server load

---

## Rate Limiting

(To be implemented) Recommended rate limits:
- Recognition endpoints: 10 requests per minute per user
- Details fetch endpoints: 100 requests per minute per user

---

## Maintenance

### Updating Worker Face Data
1. Retrain the face recognition model with updated photos
2. Restart the FaceRecognitionService
3. All recognition endpoints will use updated models

### Monitoring
- Monitor API response times (target: < 2 seconds for recognition)
- Track recognition success rates per role
- Monitor false positives/negatives

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024 | Initial release with face recognition and auto-details fetching |

---

## Support

For issues or questions, contact the development team.
