package com.waste.management.repository;

import com.waste.management.entity.Worker;
import com.waste.management.entity.WorkerRole;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Worker Repository for database operations
 */
public class WorkerRepository {
    private final SessionFactory sessionFactory;

    public WorkerRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Save worker
     *
     * @param worker Worker to save
     * @return Saved worker
     */
    public Worker save(Worker worker) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(worker);
            session.getTransaction().commit();
            return worker;
        } finally {
            session.close();
        }
    }

    /**
     * Find worker by ID
     *
     * @param id Worker ID
     * @return Optional containing worker if found
     */
    public Optional<Worker> findById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            Worker worker = session.get(Worker.class, id);
            return Optional.ofNullable(worker);
        } finally {
            session.close();
        }
    }

    /**
     * Find worker by employee ID
     *
     * @param employeeId Employee ID
     * @return Optional containing worker if found
     */
    public Optional<Worker> findByEmployeeId(String employeeId) {
        Session session = sessionFactory.openSession();
        try {
            Query<Worker> query = session.createQuery("FROM Worker WHERE employeeId = :id", Worker.class);
            query.setParameter("id", employeeId);
            return query.uniqueResultOptional();
        } finally {
            session.close();
        }
    }

    /**
     * Get all active workers
     *
     * @return List of active workers
     */
    public List<Worker> findAllActive() {
        Session session = sessionFactory.openSession();
        try {
            Query<Worker> query = session.createQuery("FROM Worker WHERE active = true", Worker.class);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Find workers by role
     *
     * @param role Worker role
     * @return List of workers with specific role
     */
    public List<Worker> findByRole(String role) {
        Session session = sessionFactory.openSession();
        try {
            Query<Worker> query = session.createQuery("FROM Worker WHERE role = :role AND active = true", Worker.class);
            query.setParameter("role", role);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Find workers by WorkerRole enum
     *
     * @param role Worker role enum
     * @return List of workers with specific role
     */
    public List<Worker> findByRole(WorkerRole role) {
        return findByRole(role.getDisplayName());
    }

    /**
     * Find all workers
     *
     * @return List of all workers
     */
    public List<Worker> findAll() {
        Session session = sessionFactory.openSession();
        try {
            Query<Worker> query = session.createQuery("FROM Worker", Worker.class);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Delete worker
     *
     * @param id Worker ID
     */
    public void deleteById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            Worker worker = session.get(Worker.class, id);
            if (worker != null) {
                session.delete(worker);
            }
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }
}
