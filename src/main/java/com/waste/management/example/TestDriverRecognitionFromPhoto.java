package com.waste.management.example;

import com.waste.management.service.FaceRecognitionWithDetailsService;
import com.waste.management.dto.WorkerDetailsDto;
import com.waste.management.repository.WorkerRepository;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * TEST: Driver Recognition from Photo with Auto-Detail Fetching
 * 
 * This test demonstrates the complete workflow:
 * 1. Load photo of driver
 * 2. Recognize the driver using face recognition (0.80 confidence threshold)
 * 3. Automatically fetch all driver details from database:
 *    - Name
 *    - Phone Number
 *    - Aadhar Number (masked for security: XXXX-XXXX-1234)
 *    - Gender (MALE/FEMALE/OTHER)
 *    - Caste
 * 4. Display formatted results
 * 
 * User Requirement:
 * "When this face appear it should recognize him as DRIVER and it should 
 *  fetch his name, phone number, aadhar number, gender and caste"
 */
public class TestDriverRecognitionFromPhoto {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("DRIVER RECOGNITION FROM PHOTO WITH AUTO-DETAIL FETCHING");
        System.out.println("=".repeat(80));
        System.out.println();

        // Initialize Hibernate SessionFactory for database access
        System.out.println("[INFO] Initializing database connection...");
        SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();

        try {
            // Create WorkerRepository for database queries
            WorkerRepository workerRepository = new WorkerRepository(sessionFactory);
            
            // Create the service that handles recognition + auto-detail-fetching
            FaceRecognitionWithDetailsService detailsService = 
                new FaceRecognitionWithDetailsService(workerRepository);

            System.out.println("[INFO] Service initialized successfully");
            System.out.println("[INFO] Recognition threshold for DRIVER: 0.80 (strict)");
            System.out.println();

            // ========================================================================
            // SCENARIO: Recognize Driver from Photo
            // ========================================================================
            
            System.out.println("-".repeat(80));
            System.out.println("STEP 1: LOAD DRIVER PHOTO");
            System.out.println("-".repeat(80));

            // Path to the driver's photo (user provided photo)
            String driverPhotoPath = "driver_photo.jpg";  // Replace with actual photo path
            
            // Verify photo exists
            if (!Files.exists(Paths.get(driverPhotoPath))) {
                System.out.println("[WARNING] Photo not found at: " + driverPhotoPath);
                System.out.println("[HINT] Please save your photo as 'driver_photo.jpg' in project root");
                System.out.println();
                driverPhotoPath = "path/to/your/driver_photo.jpg";  // Placeholder
            }

            System.out.println("[INFO] Photo path: " + driverPhotoPath);
            System.out.println();

            // ========================================================================
            // STEP 2: RECOGNIZE DRIVER FROM PHOTO
            // ========================================================================
            
            System.out.println("-".repeat(80));
            System.out.println("STEP 2: RECOGNIZE DRIVER (CONFIDENCE: 0.80)");
            System.out.println("-".repeat(80));
            System.out.println("[INFO] Processing photo...");
            System.out.println("[INFO] Extracting facial features...");
            System.out.println("[INFO] Comparing against DRIVER model (strictest threshold)...");
            System.out.println();

            // Call the service to recognize driver and fetch details
            // This method:
            // 1. Takes the photo
            // 2. Extracts facial features
            // 3. Compares against DRIVER role model (0.80 threshold)
            // 4. If matched, queries database for that driver
            // 5. Returns complete WorkerDetailsDto with all information
            
            Optional<WorkerDetailsDto> driverDetails = 
                detailsService.recognizeDriverAndFetchDetails(driverPhotoPath);

            System.out.println();
            System.out.println("-".repeat(80));
            System.out.println("STEP 3: RESULTS");
            System.out.println("-".repeat(80));
            System.out.println();

            if (driverDetails.isPresent()) {
                // ====================================================================
                // SUCCESS: Driver recognized and details fetched
                // ====================================================================
                
                WorkerDetailsDto driver = driverDetails.get();

                System.out.println("✓ DRIVER RECOGNIZED SUCCESSFULLY!");
                System.out.println();

                // Display all driver details in formatted table
                displayDriverDetails(driver);

                System.out.println();
                System.out.println("-".repeat(80));
                System.out.println("DETAILED INFORMATION");
                System.out.println("-".repeat(80));
                System.out.println();

                // Detailed breakdown of each field
                System.out.println("1. IDENTIFICATION");
                System.out.println("   └─ Worker ID: " + driver.getWorkerId());
                System.out.println("   └─ Employee ID: " + driver.getEmployeeId());
                System.out.println();

                System.out.println("2. PERSONAL INFORMATION");
                System.out.println("   └─ Full Name: " + driver.getFullName());
                System.out.println("   └─ Gender: " + driver.getGender());
                System.out.println("   └─ Caste: " + driver.getCaste());
                System.out.println();

                System.out.println("3. CONTACT DETAILS");
                System.out.println("   └─ Phone Number: " + driver.getPhoneNumber());
                System.out.println("   └─ Email: " + driver.getEmail());
                System.out.println();

                System.out.println("4. SECURITY DETAILS");
                System.out.println("   └─ Aadhar Number (Masked): " + driver.getAadharNumber());
                System.out.println("      ⚠ Note: Only last 4 digits visible for security");
                System.out.println();

                System.out.println("5. EMPLOYMENT DETAILS");
                System.out.println("   └─ Role: " + driver.getRole());
                System.out.println("   └─ Department: " + driver.getDepartment());
                System.out.println("   └─ Active: " + driver.isActive());
                System.out.println();

                System.out.println("6. RECOGNITION CONFIDENCE");
                System.out.println("   └─ Match Confidence: " + 
                    String.format("%.2f%%", driver.getFaceMatchConfidence() * 100));
                System.out.println("   └─ Required Threshold: 0.80 (80%)");
                System.out.println();

            } else {
                // ====================================================================
                // FAILURE: Driver not recognized
                // ====================================================================
                
                System.out.println("✗ DRIVER NOT RECOGNIZED");
                System.out.println();
                System.out.println("Possible reasons:");
                System.out.println("  1. Photo quality too poor");
                System.out.println("  2. Face not clearly visible");
                System.out.println("  3. Driver not registered in system");
                System.out.println("  4. Confidence below 0.80 threshold");
                System.out.println();
                System.out.println("NEXT STEPS:");
                System.out.println("  • Use a clear, well-lit photo");
                System.out.println("  • Ensure driver is registered in database");
                System.out.println("  • Check that reference photo is stored correctly");
                System.out.println("  • Try adjusting photo angle or lighting");
                System.out.println();
            }

            System.out.println();
            System.out.println("=".repeat(80));
            System.out.println("TEST COMPLETE");
            System.out.println("=".repeat(80));

        } catch (Exception e) {
            System.err.println("[ERROR] Exception occurred during driver recognition:");
            e.printStackTrace();
        } finally {
            // Clean up resources
            sessionFactory.close();
        }
    }

    /**
     * Display driver details in a formatted table
     */
    private static void displayDriverDetails(WorkerDetailsDto driver) {
        System.out.println("┌" + "─".repeat(78) + "┐");
        System.out.println("│ " + String.format("%-76s", "DRIVER INFORMATION") + " │");
        System.out.println("├" + "─".repeat(78) + "┤");
        
        // Row 1: Name
        System.out.println("│ " + String.format("%-37s", "Name: " + driver.getFullName()) + 
                          " │ " + String.format("%-37s", "Phone: " + driver.getPhoneNumber()) + " │");
        
        // Row 2: Aadhar and Gender
        System.out.println("│ " + String.format("%-37s", "Aadhar: " + driver.getAadharNumber()) + 
                          " │ " + String.format("%-37s", "Gender: " + driver.getGender()) + " │");
        
        // Row 3: Caste and Role
        System.out.println("│ " + String.format("%-37s", "Caste: " + driver.getCaste()) + 
                          " │ " + String.format("%-37s", "Role: " + driver.getRole()) + " │");
        
        // Row 4: Confidence
        System.out.println("│ " + String.format("%-76s", 
                          "Recognition Confidence: " + String.format("%.2f%%", driver.getFaceMatchConfidence() * 100)) + " │");
        
        System.out.println("└" + "─".repeat(78) + "┘");
    }

    /**
     * INTEGRATION WITH REAL SYSTEM:
     * 
     * To use this in production:
     * 
     * 1. Prepare driver photo:
     *    - Save as 'driver_photo.jpg' in project root
     *    - Or update driverPhotoPath variable with actual path
     * 
     * 2. Ensure driver exists in database with:
     *    - Full Name
     *    - Phone Number
     *    - Aadhar Number
     *    - Gender
     *    - Caste
     *    - Reference face image stored
     * 
     * 3. Run this test:
     *    mvn clean compile
     *    mvn exec:java@test-driver-recognition
     * 
     * 4. Verify all 5 details are returned:
     *    ✓ Name
     *    ✓ Phone Number
     *    ✓ Aadhar Number (masked)
     *    ✓ Gender
     *    ✓ Caste
     * 
     * EXPECTED OUTPUT:
     * ✓ DRIVER RECOGNIZED SUCCESSFULLY!
     * ├─ Name: [Driver's Name]
     * ├─ Phone: [Driver's Phone]
     * ├─ Aadhar: XXXX-XXXX-[last 4 digits]
     * ├─ Gender: MALE/FEMALE/OTHER
     * └─ Caste: [Driver's Caste]
     */
}
