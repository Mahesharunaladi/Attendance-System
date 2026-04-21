package com.waste.management.example;

import com.waste.management.config.HibernateConfig;
import com.waste.management.dto.WorkerDetailsDto;
import com.waste.management.entity.WorkerRole;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.service.FaceRecognitionWithDetailsService;

import java.util.List;
import java.util.Optional;

/**
 * Example demonstrating face recognition with automatic worker details fetching
 * 
 * Workflow:
 * 1. Load a captured image (driver/worker photo)
 * 2. Recognize the face using role-specific thresholds
 * 3. Automatically fetch worker details (name, phone, Aadhar, gender, caste)
 * 4. Display the complete worker information
 */
public class FaceRecognitionDetailsFetchingExample {

    private static FaceRecognitionWithDetailsService detailsFetchingService;
    private static WorkerRepository workerRepository;

    public static void main(String[] args) {
        try {
            System.out.println("=".repeat(100));
            System.out.println("FACE RECOGNITION WITH AUTOMATIC WORKER DETAILS FETCHING");
            System.out.println("=".repeat(100));
            System.out.println();

            // Initialize services
            initializeServices();

            // Example 1: Recognize driver and fetch details
            example1_RecognizeDriverAndFetchDetails();

            // Example 2: Recognize worker by specific role and fetch details
            example2_RecognizeWorkerByRoleAndFetchDetails();

            // Example 3: Auto-detect role and fetch details
            example3_AutoDetectRoleAndFetchDetails();

            // Example 4: Fetch details without recognition (by ID)
            example4_FetchWorkerDetailsById();

            // Example 5: Fetch all driver details at once
            example5_FetchAllDriverDetails();

            // Example 6: Simulate multi-worker recognition (batch processing)
            example6_BatchWorkerRecognition();

        } catch (Exception e) {
            System.err.println("Error in example: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize the services
     */
    private static void initializeServices() {
        System.out.println("📋 Initializing services...");
        
        workerRepository = new WorkerRepository(HibernateConfig.getSessionFactory());
        detailsFetchingService = new FaceRecognitionWithDetailsService(workerRepository);
        
        System.out.println("✅ Services initialized successfully\n");
    }

    /**
     * Example 1: Recognize driver from captured image and fetch complete details
     */
    private static void example1_RecognizeDriverAndFetchDetails() {
        System.out.println("━".repeat(100));
        System.out.println("EXAMPLE 1: Recognize Driver and Fetch Details");
        System.out.println("━".repeat(100));
        
        try {
            String capturedImagePath = "path/to/captured/driver/image.jpg";
            System.out.println("📸 Captured Image Path: " + capturedImagePath);
            System.out.println("🔍 Performing driver face recognition with 0.80 threshold...");
            System.out.println();

            // Recognize driver and fetch details
            Optional<WorkerDetailsDto> driverDetails = detailsFetchingService.recognizeDriverAndFetchDetails(capturedImagePath);

            if (driverDetails.isPresent()) {
                System.out.println("✅ Driver recognized successfully!");
                System.out.println();
                displayWorkerDetails(driverDetails.get());
            } else {
                System.out.println("❌ Driver not recognized. Face does not match any registered driver.");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
        System.out.println("\n");
    }

    /**
     * Example 2: Recognize worker by specific role and fetch details
     */
    private static void example2_RecognizeWorkerByRoleAndFetchDetails() {
        System.out.println("━".repeat(100));
        System.out.println("EXAMPLE 2: Recognize Worker by Specific Role and Fetch Details");
        System.out.println("━".repeat(100));
        
        try {
            String capturedImagePath = "path/to/captured/worker/image.jpg";
            WorkerRole workerRole = WorkerRole.CLEANER;
            
            System.out.println("📸 Captured Image Path: " + capturedImagePath);
            System.out.println("👤 Looking for role: " + workerRole.getDisplayName());
            System.out.println("🔍 Performing face recognition with role-specific threshold...");
            System.out.println();

            // Recognize worker by role
            Optional<WorkerDetailsDto> workerDetails = detailsFetchingService.recognizeWorkerAndFetchDetails(
                capturedImagePath, 
                workerRole
            );

            if (workerDetails.isPresent()) {
                System.out.println("✅ Worker recognized successfully!");
                System.out.println();
                displayWorkerDetails(workerDetails.get());
            } else {
                System.out.println("❌ No matching worker found for role: " + workerRole.getDisplayName());
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
        System.out.println("\n");
    }

    /**
     * Example 3: Auto-detect worker role and fetch details
     */
    private static void example3_AutoDetectRoleAndFetchDetails() {
        System.out.println("━".repeat(100));
        System.out.println("EXAMPLE 3: Auto-Detect Role and Fetch Details");
        System.out.println("━".repeat(100));
        
        try {
            String capturedImagePath = "path/to/captured/any/worker/image.jpg";
            
            System.out.println("📸 Captured Image Path: " + capturedImagePath);
            System.out.println("🔍 Attempting to recognize face across all roles...");
            System.out.println("   Testing: DRIVER(0.80) → SUPERVISOR(0.78) → MANAGER(0.78) → CLEANER(0.75) → HELPER(0.75)");
            System.out.println();

            // Auto-detect role and fetch details (tries all roles)
            Optional<WorkerDetailsDto> workerDetails = detailsFetchingService.recognizeAnyWorkerAndFetchDetails(
                capturedImagePath
            );

            if (workerDetails.isPresent()) {
                System.out.println("✅ Worker recognized successfully!");
                System.out.println();
                displayWorkerDetails(workerDetails.get());
            } else {
                System.out.println("❌ Could not recognize worker face against any registered workers.");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
        System.out.println("\n");
    }

    /**
     * Example 4: Fetch worker details directly by ID (without recognition)
     */
    private static void example4_FetchWorkerDetailsById() {
        System.out.println("━".repeat(100));
        System.out.println("EXAMPLE 4: Fetch Worker Details by ID (No Recognition)");
        System.out.println("━".repeat(100));
        
        try {
            Long workerId = 1L;
            
            System.out.println("🔎 Fetching worker details for ID: " + workerId);
            System.out.println();

            Optional<WorkerDetailsDto> workerDetails = detailsFetchingService.getWorkerDetailsById(workerId);

            if (workerDetails.isPresent()) {
                System.out.println("✅ Worker details retrieved successfully!");
                System.out.println();
                displayWorkerDetails(workerDetails.get());
            } else {
                System.out.println("❌ No worker found with ID: " + workerId);
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
        System.out.println("\n");
    }

    /**
     * Example 5: Fetch all driver details at once
     */
    private static void example5_FetchAllDriverDetails() {
        System.out.println("━".repeat(100));
        System.out.println("EXAMPLE 5: Fetch All Driver Details");
        System.out.println("━".repeat(100));
        
        try {
            System.out.println("📋 Fetching all registered drivers...");
            System.out.println();

            List<WorkerDetailsDto> allDrivers = detailsFetchingService.getAllDriverDetails();

            if (!allDrivers.isEmpty()) {
                System.out.println("✅ Retrieved " + allDrivers.size() + " driver(s):");
                System.out.println();

                for (int i = 0; i < allDrivers.size(); i++) {
                    System.out.println(String.format("─ Driver #%d:", i + 1));
                    displayWorkerDetails(allDrivers.get(i));
                    if (i < allDrivers.size() - 1) {
                        System.out.println();
                    }
                }
            } else {
                System.out.println("❌ No registered drivers found in the system.");
            }

        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
        System.out.println("\n");
    }

    /**
     * Example 6: Simulate batch processing - recognize multiple workers
     */
    private static void example6_BatchWorkerRecognition() {
        System.out.println("━".repeat(100));
        System.out.println("EXAMPLE 6: Batch Worker Recognition (Multiple Images)");
        System.out.println("━".repeat(100));
        
        try {
            String[] imagePaths = {
                "path/to/worker1.jpg",
                "path/to/worker2.jpg",
                "path/to/worker3.jpg"
            };

            System.out.println("📦 Processing " + imagePaths.length + " captured images...");
            System.out.println();

            int successCount = 0;
            int failureCount = 0;

            for (int i = 0; i < imagePaths.length; i++) {
                System.out.println(String.format("Processing image %d/%d: %s", i + 1, imagePaths.length, imagePaths[i]));
                
                try {
                    Optional<WorkerDetailsDto> workerDetails = detailsFetchingService.recognizeAnyWorkerAndFetchDetails(
                        imagePaths[i]
                    );

                    if (workerDetails.isPresent()) {
                        System.out.println("  ✅ Recognized: " + workerDetails.get().getFullName() + 
                                         " (" + workerDetails.get().getRole() + 
                                         ", Confidence: " + String.format("%.2f%%", workerDetails.get().getFaceMatchConfidence()) + ")");
                        successCount++;
                    } else {
                        System.out.println("  ❌ Not recognized");
                        failureCount++;
                    }
                } catch (Exception e) {
                    System.out.println("  ⚠️ Error processing: " + e.getMessage());
                    failureCount++;
                }
                System.out.println();
            }

            System.out.println("📊 Batch Processing Summary:");
            System.out.println("   Total: " + imagePaths.length);
            System.out.println("   ✅ Recognized: " + successCount);
            System.out.println("   ❌ Failed: " + failureCount);
            System.out.println("   Success Rate: " + String.format("%.1f%%", (successCount * 100.0 / imagePaths.length)));

        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
        System.out.println("\n");
    }

    /**
     * Display worker details in formatted output
     */
    private static void displayWorkerDetails(WorkerDetailsDto details) {
        System.out.println("┌" + "─".repeat(98) + "┐");
        System.out.println("│ " + String.format("%-96s", "WORKER DETAILS") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        
        System.out.println("│ " + String.format("%-96s", "Personal Information") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printDetail("  Full Name", details.getFullName());
        printDetail("  Worker ID", String.valueOf(details.getWorkerId()));
        printDetail("  Employee ID", details.getEmployeeId());
        printDetail("  Gender", details.getGender() != null ? details.getGender().getDisplayName() : "N/A");
        
        System.out.println("│ " + String.format("%-96s", "") + " │");
        System.out.println("│ " + String.format("%-96s", "Contact Information") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printDetail("  Phone Number", details.getPhoneNumber());
        printDetail("  Email", details.getEmail());
        
        System.out.println("│ " + String.format("%-96s", "") + " │");
        System.out.println("│ " + String.format("%-96s", "Identity & Role") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printDetail("  Aadhar Number", maskAadhar(details.getAadharNumber()));
        printDetail("  Caste", details.getCaste());
        printDetail("  Role", details.getRole() != null ? details.getRole().getDisplayName() : "N/A");
        printDetail("  Department", details.getDepartment());
        
        System.out.println("│ " + String.format("%-96s", "") + " │");
        System.out.println("│ " + String.format("%-96s", "Face Recognition Data") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printDetail("  Face Match Confidence", String.format("%.2f%%", details.getFaceMatchConfidence()));
        printDetail("  Status", details.isActive() ? "Active" : "Inactive");
        printDetail("  Message", details.getMessage());
        
        System.out.println("└" + "─".repeat(98) + "┘");
    }

    /**
     * Print a detail line
     */
    private static void printDetail(String label, String value) {
        System.out.println(String.format("│ %-30s: %-63s │", label, value == null ? "N/A" : value));
    }

    /**
     * Mask Aadhar number for security (show only last 4 digits)
     */
    private static String maskAadhar(String aadhar) {
        if (aadhar == null || aadhar.length() < 4) {
            return "****";
        }
        return "XXXX-XXXX-" + aadhar.substring(aadhar.length() - 4);
    }
}
