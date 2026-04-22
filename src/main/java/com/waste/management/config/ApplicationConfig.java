package com.waste.management.config;

import com.waste.management.repository.AttendanceRepository;
import com.waste.management.repository.WorkerRepository;
import com.waste.management.repository.WasteTaskRepository;
import com.waste.management.service.AttendanceService;
import com.waste.management.service.FaceRecognitionService;
import com.waste.management.service.WasteManagementService;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for Service and Beans
 * Controllers are auto-registered via @RestController annotation
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public SessionFactory sessionFactory() {
        return HibernateConfig.getSessionFactory();
    }

    @Bean
    public WorkerRepository workerRepository(SessionFactory sessionFactory) {
        return new WorkerRepository(sessionFactory);
    }

    @Bean
    public AttendanceRepository attendanceRepository(SessionFactory sessionFactory) {
        return new AttendanceRepository(sessionFactory);
    }

    @Bean
    public WasteTaskRepository wasteTaskRepository(SessionFactory sessionFactory) {
        return new WasteTaskRepository(sessionFactory);
    }

    @Bean
    public FaceRecognitionService faceRecognitionService() {
        return new FaceRecognitionService();
    }

    @Bean
    public AttendanceService attendanceService(AttendanceRepository attendanceRepository,
                                              FaceRecognitionService faceRecognitionService,
                                              WorkerRepository workerRepository) {
        return new AttendanceService(attendanceRepository, faceRecognitionService, workerRepository);
    }

    @Bean
    public WasteManagementService wasteManagementService(WasteTaskRepository wasteTaskRepository) {
        return new WasteManagementService(wasteTaskRepository);
    }
}

