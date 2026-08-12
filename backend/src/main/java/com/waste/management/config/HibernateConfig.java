package com.waste.management.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Hibernate Configuration and SessionFactory setup
 */
public class HibernateConfig {
    private static SessionFactory sessionFactory;

    static {
        try {
            // Use H2 embedded database by default for Spring Boot
            try {
                StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                        .configure("hibernate-h2.cfg.xml")
                        .build();
                MetadataSources metadataSources = new MetadataSources(registry);
                Metadata metadata = metadataSources.buildMetadata();
                sessionFactory = metadata.getSessionFactoryBuilder().build();
                System.out.println("Successfully initialized Hibernate with H2 embedded database");
            } catch (Exception h2Exception) {
                System.out.println("H2 initialization failed, trying MySQL...");
                // Fall back to MySQL
                try {
                    StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                            .configure("hibernate.cfg.xml")
                            .build();
                    MetadataSources metadataSources = new MetadataSources(registry);
                    Metadata metadata = metadataSources.buildMetadata();
                    sessionFactory = metadata.getSessionFactoryBuilder().build();
                    System.out.println("Successfully initialized Hibernate with MySQL");
                } catch (Exception mysqlException) {
                    System.err.println("Both H2 and MySQL initialization failed!");
                    throw mysqlException;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize Hibernate SessionFactory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get SessionFactory
     *
     * @return Configured SessionFactory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Close SessionFactory
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
