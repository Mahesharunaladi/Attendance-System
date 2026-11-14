INSERT INTO worker (
    employee_id,
    full_name,
    phone_number,
    email,
    role,
    aadhar_number,
    gender,
    caste,
    department,
    active,
    facial_data_path,
    created_at
) VALUES (
    'DRV001',                           -- Your unique driver ID
    'Your Driver Name',                 -- Your name
    '9876543210',                       -- Your phone number
    'driver@example.com',               -- Your email
    'DRIVER',                           -- MUST be 'DRIVER' for strict 0.80 threshold
    '1234-5678-9012',                   -- Your Aadhar number
    'MALE',                             -- MALE / FEMALE / OTHER
    'Your Caste Name',                  -- Your caste
    'Transportation',                   -- Department
    true,                               -- Active: true/false
    '/path/to/driver_reference.jpg',   -- Path to your reference photo
    NOW()
);

-- ============================================================================
-- STEP 2: UPDATE DRIVER DETAILS (if already exists)
-- ============================================================================

UPDATE worker SET
    full_name = 'Your Driver Name',
    phone_number = '9876543210',
    aadhar_number = '1234-5678-9012',
    gender = 'MALE',
    caste = 'Your Caste Name',
    facial_data_path = '/path/to/driver_reference.jpg',
    active = true
WHERE employee_id = 'DRV001';

-- ============================================================================
-- STEP 3: VERIFY DRIVER EXISTS
-- ============================================================================

SELECT 
    id,
    employee_id,
    full_name,
    phone_number,
    role,
    aadhar_number,
    gender,
    caste,
    facial_data_path,
    active
FROM worker
WHERE employee_id = 'DRV001' AND role = 'DRIVER';

-- Expected output:
-- ┌────┬─────────────┬──────────────────┬──────────────┬────────┬─────────────┬────────┬──────────┬──────────────────────────────┬────────┐
-- │ id │employee_id  │full_name         │phone_number  │role    │aadhar_number│gender  │caste     │facial_data_path              │active  │
-- ├────┼─────────────┼──────────────────┼──────────────┼────────┼─────────────┼────────┼──────────┼──────────────────────────────┼────────┤
-- │ 1  │ DRV001      │Your Driver Name  │9876543210    │DRIVER  │1234-...     │MALE    │Caste Name│/path/to/driver_reference.jpg │true    │
-- └────┴─────────────┴──────────────────┴──────────────┴────────┴─────────────┴────────┴──────────┴──────────────────────────────┴────────┘

-- ============================================================================
-- STEP 4: GET ALL DRIVERS (for verification)
-- ============================================================================

SELECT 
    id,
    employee_id,
    full_name,
    phone_number,
    role,
    gender,
    caste,
    active
FROM worker
WHERE role = 'DRIVER' AND active = true
ORDER BY created_at DESC;

-- ============================================================================
-- STEP 5: DELETE DRIVER (if needed)
-- ============================================================================

DELETE FROM worker WHERE employee_id = 'DRV001';

-- ============================================================================
-- TEST DATA: Multiple Drivers
-- ============================================================================
-- Uncomment and modify to add multiple test drivers

/*
-- Driver 1
INSERT INTO worker (employee_id, full_name, phone_number, email, role, aadhar_number, gender, caste, department, active, facial_data_path, created_at)
VALUES ('DRV001', 'Rajesh Kumar', '9876543210', 'rajesh@example.com', 'DRIVER', '1234-5678-9012', 'MALE', 'Tamil', 'Transportation', true, '/photos/driver1.jpg', NOW());

-- Driver 2
INSERT INTO worker (employee_id, full_name, phone_number, email, role, aadhar_number, gender, caste, department, active, facial_data_path, created_at)
VALUES ('DRV002', 'Priya Singh', '9876543211', 'priya@example.com', 'DRIVER', '1234-5678-9013', 'FEMALE', 'Punjabi', 'Transportation', true, '/photos/driver2.jpg', NOW());

-- Driver 3
INSERT INTO worker (employee_id, full_name, phone_number, email, role, aadhar_number, gender, caste, department, active, facial_data_path, created_at)
VALUES ('DRV003', 'Amit Sharma', '9876543212', 'amit@example.com', 'DRIVER', '1234-5678-9014', 'MALE', 'Marathi', 'Transportation', true, '/photos/driver3.jpg', NOW());
*/

-- ============================================================================
-- IMPORTANT NOTES
-- ============================================================================

-- 1. ROLE MUST BE 'DRIVER'
--    The system uses specific thresholds for each role:
--    - DRIVER: 0.80 (strictest, for vehicle operation)
--    - SUPERVISOR: 0.78
--    - MANAGER: 0.78
--    - CLEANER: 0.75
--    - HELPER: 0.75

-- 2. GENDER VALUES
--    Valid values: 'MALE', 'FEMALE', 'OTHER'

-- 3. AADHAR NUMBER
--    - Stored as: 1234-5678-9012
--    - API returns as: XXXX-XXXX-9012 (masked for security)

-- 4. FACIAL DATA PATH
--    - Path to reference face image on server
--    - Image should be clear, frontal, and well-lit
--    - Format: JPG or PNG
--    - Size: Minimum 100x100 pixels

-- 5. ACTIVE STATUS
--    - true: Driver can be recognized
--    - false: Driver will be skipped during recognition

-- ============================================================================
-- RECOGNITION FLOW
-- ============================================================================

-- When you call TestDriverRecognitionFromPhoto:
--
-- 1. System loads your photo
-- 2. System loads all ACTIVE DRIVER records
-- 3. System compares your photo against all driver reference photos
-- 4. System uses 0.80 confidence threshold (strictest)
-- 5. If match found with confidence >= 0.80:
--    - Fetches: full_name
--    - Fetches: phone_number
--    - Fetches: aadhar_number (masked to XXXX-XXXX-last 4)
--    - Fetches: gender
--    - Fetches: caste
--    - Returns in WorkerDetailsDto
-- 6. If no match OR confidence < 0.80:
--    - Returns "Driver not recognized"

-- ============================================================================
-- EXPECTED RESPONSE (when face matches)
-- ============================================================================

/*
{
  "success": true,
  "data": {
    "workerId": 1,
    "employeeId": "DRV001",
    "fullName": "Your Driver Name",
    "phoneNumber": "9876543210",
    "email": "driver@example.com",
    "role": "DRIVER",
    "aadharNumber": "XXXX-XXXX-9012",     ← Masked for security
    "gender": "MALE",
    "caste": "Your Caste Name",
    "department": "Transportation",
    "active": true,
    "faceMatchConfidence": 0.8532
  },
  "message": "Driver recognized and details fetched"
}
*/

-- ============================================================================
-- TROUBLESHOOTING
-- ============================================================================

-- Check if driver exists:
SELECT COUNT(*) as driver_count FROM worker WHERE role = 'DRIVER' AND active = true;

-- Check if aadhar is stored correctly:
SELECT full_name, aadhar_number FROM worker WHERE employee_id = 'DRV001';

-- Check if facial_data_path is set:
SELECT full_name, facial_data_path FROM worker WHERE employee_id = 'DRV001';

-- Check all driver details:
SELECT 
    id,
    employee_id,
    full_name,
    phone_number,
    aadhar_number,
    gender,
    caste,
    facial_data_path,
    active
FROM worker
WHERE role = 'DRIVER'
LIMIT 10;

-- ============================================================================
-- SECURITY NOTES
-- ============================================================================

-- 1. Aadhar numbers are stored in plain text in database (in production, consider encryption)
-- 2. API returns masked Aadhar: XXXX-XXXX-last4digits
-- 3. Only authorized personnel should have access to this data
-- 4. Use HTTPS for all API calls in production
-- 5. Implement proper authentication/authorization

-- ============================================================================
