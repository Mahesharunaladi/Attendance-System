package com.waste.management.example;

import com.waste.management.dto.WorkerDetailsDto;
import com.waste.management.service.FaceRecognitionWithDetailsService;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.config.HibernateConfig;

import java.util.Optional;

/**
 * Simple Test: Recognize Worker from Photo and Fetch All Details
 * 
 * This example demonstrates:
 * 1. Load worker photo
 * 2. Recognize the person (auto-detect role)
 * 3. Fetch complete details: name, phone, Aadhar, gender, caste
 * 4. Display all information
 */
public class TestWorkerRecognitionFromPhoto {

    public static void main(String[] args) {
        try {
            System.out.println("\n" + "=".repeat(100));
            System.out.println("WORKER RECOGNITION TEST - Auto Fetch Details (Name, Phone, Aadhar, Gender, Caste)");
            System.out.println("=".repeat(100) + "\n");

            // Initialize services
            System.out.println("📋 Initializing services...");
            WorkerRepository workerRepository = new WorkerRepository(HibernateConfig.getSessionFactory());
            FaceRecognitionWithDetailsService detailsService = 
                new FaceRecognitionWithDetailsService(workerRepository);
            System.out.println("✅ Services initialized\n");

            // Path to the uploaded photo
            // In real scenario, this would be the path where the photo is saved after upload
            String photoPath = "path/to/worker_photo.jpg"; // Replace with actual photo path
            
            System.out.println("📸 STEP 1: Load Worker Photo");
            System.out.println("   Photo Path: " + photoPath);
            System.out.println("   Status: Ready for recognition\n");

            System.out.println("🔍 STEP 2: Recognize Worker (Auto-Detect Role)");
            System.out.println("   Testing face against all registered workers...");
            System.out.println("   Priority: DRIVER (0.80) → SUPERVISOR (0.78) → MANAGER (0.78) → CLEANER (0.75) → HELPER (0.75)\n");

            // Recognize worker and fetch details
            Optional<WorkerDetailsDto> workerDetails = detailsService.recognizeAnyWorkerAndFetchDetails(photoPath);

            if (workerDetails.isPresent()) {
                System.out.println("✅ RECOGNITION SUCCESSFUL!\n");
                
                WorkerDetailsDto worker = workerDetails.get();
                
                System.out.println("📋 STEP 3: Fetched Worker Details\n");
                
                displayWorkerDetails(worker);
                
                System.out.println("\n✨ ALL DETAILS FETCHED SUCCESSFULLY!\n");
                
                // Summary
                System.out.println("📊 SUMMARY:");
                System.out.println("   ✓ Face recognized with " + String.format("%.2f%%", worker.getFaceMatchConfidence()) + " confidence");
                System.out.println("   ✓ Worker: " + worker.getFullName());
                System.out.println("   ✓ Role: " + worker.getRole().getDisplayName());
                System.out.println("   ✓ Phone: " + worker.getPhoneNumber());
                System.out.println("   ✓ Aadhar: " + maskAadhar(worker.getAadharNumber()));
                System.out.println("   ✓ Gender: " + (worker.getGender() != null ? worker.getGender().getDisplayName() : "N/A"));
                System.out.println("   ✓ Caste: " + worker.getCaste());
                System.out.println("   ✓ Department: " + worker.getDepartment());
                
            } else {
                System.out.println("❌ RECOGNITION FAILED!");
                System.out.println("   Face does not match any registered worker.\n");
                System.out.println("   Possible reasons:");
                System.out.println("   • Photo quality is poor (too dark, blurry, or at wrong angle)");
                System.out.println("   • Worker is not registered in the system");
                System.out.println("   • Face doesn't meet confidence threshold");
                System.out.println("   • Reference face images are missing\n");
                
                System.out.println("💡 Solution:");
                System.out.println("   1. Ensure worker is registered in database");
                System.out.println("   2. Upload clear, front-facing photo with good lighting");
                System.out.println("   3. Ensure reference face images exist in system");
                System.out.println("   4. Try again with better quality photo\n");
            }

            System.out.println("=".repeat(100));
            System.out.println("Test Complete");
            System.out.println("=".repeat(100) + "\n");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();
        }
    }

    /**
     * Display worker details in formatted output
     */
    private static void displayWorkerDetails(WorkerDetailsDto worker) {
        System.out.println("┌" + "─".repeat(98) + "┐");
        System.out.println("│ " + String.format("%-96s", "RECOGNIZED WORKER DETAILS") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        
        // Personal Information
        System.out.println("│ " + String.format("%-96s", "PERSONAL INFORMATION") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printField("Full Name", worker.getFullName());
        printField("Worker ID", String.valueOf(worker.getWorkerId()));
        printField("Employee ID", worker.getEmployeeId());
        
        // Contact Information
        System.out.println("│ " + String.format("%-96s", "") + " │");
        System.out.println("│ " + String.format("%-96s", "CONTACT INFORMATION") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printField("Phone Number", worker.getPhoneNumber());
        printField("Email", worker.getEmail());
        
        // Identity & Role
        System.out.println("│ " + String.format("%-96s", "") + " │");
        System.out.println("│ " + String.format("%-96s", "IDENTITY & ROLE INFORMATION") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printField("Aadhar Number", maskAadhar(worker.getAadharNumber()) + " [MASKED]");
        printField("Gender", worker.getGender() != null ? worker.getGender().getDisplayName() : "N/A");
        printField("Caste", worker.getCaste());
        printField("Role", worker.getRole() != null ? worker.getRole().getDisplayName() : "N/A");
        printField("Department", worker.getDepartment());
        
        // Recognition Data
        System.out.println("│ " + String.format("%-96s", "") + " │");
        System.out.println("│ " + String.format("%-96s", "FACE RECOGNITION DATA") + " │");
        System.out.println("├" + "─".repeat(98) + "┤");
        printField("Face Match Confidence", String.format("%.2f%%", worker.getFaceMatchConfidence()));
        printField("Status", worker.isActive() ? "ACTIVE ✓" : "INACTIVE ✗");
        printField("Message", worker.getMessage());
        
        System.out.println("└" + "─".repeat(98) + "┘");
    }

    /**
     * Print a detail field
     */
    private static void printField(String label, String value) {
        System.out.println(String.format("│ %-30s: %-63s │", label, value == null ? "N/A" : value));
    }

    /**
     * Mask Aadhar number for security (show only last 4 digits)
     */
    private static String maskAadhar(String aadhar) {
        if (aadhar == null || aadhar.length() < 4) {
            return "****-****-****";
        }
        return "XXXX-XXXX-" + aadhar.substring(aadhar.length() - 4);
    }
}
