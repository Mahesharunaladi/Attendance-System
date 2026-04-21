package com.waste.management.repository;

import com.waste.management.entity.WasteTask;
import com.waste.management.entity.TaskStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Waste Task Repository for database operations
 */
public class WasteTaskRepository {
    private final SessionFactory sessionFactory;

    public WasteTaskRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Save waste task
     *
     * @param task Waste task to save
     * @return Saved task
     */
    public WasteTask save(WasteTask task) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(task);
            session.getTransaction().commit();
            return task;
        } finally {
            session.close();
        }
    }

    /**
     * Find task by ID
     *
     * @param id Task ID
     * @return Optional containing task if found
     */
    public Optional<WasteTask> findById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            WasteTask task = session.get(WasteTask.class, id);
            return Optional.ofNullable(task);
        } finally {
            session.close();
        }
    }

    /**
     * Find all tasks by status
     *
     * @param status Task status
     * @return List of tasks
     */
    public List<WasteTask> findByStatus(TaskStatus status) {
        Session session = sessionFactory.openSession();
        try {
            Query<WasteTask> query = session.createQuery(
                    "FROM WasteTask WHERE status = :status ORDER BY scheduledDate DESC",
                    WasteTask.class
            );
            query.setParameter("status", status);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Find tasks assigned to worker
     *
     * @param workerId Worker ID
     * @return List of tasks
     */
    public List<WasteTask> findByAssignedWorker(Long workerId) {
        Session session = sessionFactory.openSession();
        try {
            Query<WasteTask> query = session.createQuery(
                    "FROM WasteTask WHERE assignedWorker.id = :workerId " +
                    "ORDER BY scheduledDate DESC",
                    WasteTask.class
            );
            query.setParameter("workerId", workerId);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Find tasks assigned to driver
     *
     * @param driverId Driver ID
     * @return List of tasks
     */
    public List<WasteTask> findByDriver(Long driverId) {
        Session session = sessionFactory.openSession();
        try {
            Query<WasteTask> query = session.createQuery(
                    "FROM WasteTask WHERE driver.id = :driverId " +
                    "ORDER BY scheduledDate DESC",
                    WasteTask.class
            );
            query.setParameter("driverId", driverId);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Count total tasks
     *
     * @return Total count
     */
    public long countTotal() {
        Session session = sessionFactory.openSession();
        try {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(id) FROM WasteTask",
                    Long.class
            );
            Long result = query.uniqueResult();
            return result != null ? result : 0;
        } finally {
            session.close();
        }
    }

    /**
     * Count tasks by status
     *
     * @param status Task status
     * @return Count
     */
    public long countByStatus(TaskStatus status) {
        Session session = sessionFactory.openSession();
        try {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(id) FROM WasteTask WHERE status = :status",
                    Long.class
            );
            query.setParameter("status", status);
            Long result = query.uniqueResult();
            return result != null ? result : 0;
        } finally {
            session.close();
        }
    }

    /**
     * Sum actual waste weight
     *
     * @return Total weight
     */
    public Double sumActualWeight() {
        Session session = sessionFactory.openSession();
        try {
            Query<Double> query = session.createQuery(
                    "SELECT COALESCE(SUM(actualWeight), 0.0) FROM WasteTask WHERE status = :status",
                    Double.class
            );
            query.setParameter("status", TaskStatus.COMPLETED);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        } finally {
            session.close();
        }
    }

    /**
     * Delete task
     *
     * @param id Task ID
     */
    public void deleteById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            WasteTask task = session.get(WasteTask.class, id);
            if (task != null) {
                session.delete(task);
            }
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }
}
