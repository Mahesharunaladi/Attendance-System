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
            // Create service registry
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml")
                    .build();

            // Create metadata sources
            MetadataSources metadataSources = new MetadataSources(registry);

            // Build metadata
            Metadata metadata = metadataSources.buildMetadata();

            // Create session factory
            sessionFactory = metadata.getSessionFactoryBuilder().build();
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
