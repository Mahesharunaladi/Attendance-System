package com.waste.management.repository;

import com.waste.management.entity.AttendanceRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Attendance Repository for database operations
 */
public class AttendanceRepository {
    private final SessionFactory sessionFactory;

    public AttendanceRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Save attendance record
     *
     * @param record Attendance record to save
     * @return Saved record
     */
    public AttendanceRecord save(AttendanceRecord record) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(record);
            session.getTransaction().commit();
            return record;
        } finally {
            session.close();
        }
    }

    /**
     * Find today's check-in by worker
     *
     * @param workerId Worker ID
     * @param date Date to check
     * @return Optional containing record if found
     */
    public Optional<AttendanceRecord> findTodayCheckInByWorker(Long workerId, LocalDate date) {
        Session session = sessionFactory.openSession();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            Query<AttendanceRecord> query = session.createQuery(
                    "FROM AttendanceRecord WHERE worker.id = :workerId " +
                    "AND checkInTime >= :startOfDay AND checkInTime < :endOfDay",
                    AttendanceRecord.class
            );
            query.setParameter("workerId", workerId);
            query.setParameter("startOfDay", startOfDay);
            query.setParameter("endOfDay", endOfDay);
            return query.uniqueResultOptional();
        } finally {
            session.close();
        }
    }

    /**
     * Find records within date range
     *
     * @param startDate Start date time
     * @param endDate End date time
     * @return List of records
     */
    public List<AttendanceRecord> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Session session = sessionFactory.openSession();
        try {
            Query<AttendanceRecord> query = session.createQuery(
                    "FROM AttendanceRecord WHERE checkInTime BETWEEN :start AND :end " +
                    "ORDER BY checkInTime DESC",
                    AttendanceRecord.class
            );
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Find records for specific worker within date range
     *
     * @param workerId Worker ID
     * @param startDate Start date time
     * @param endDate End date time
     * @return List of records
     */
    public List<AttendanceRecord> findByWorkerAndDateRange(Long workerId, LocalDateTime startDate, LocalDateTime endDate) {
        Session session = sessionFactory.openSession();
        try {
            Query<AttendanceRecord> query = session.createQuery(
                    "FROM AttendanceRecord WHERE worker.id = :workerId " +
                    "AND checkInTime BETWEEN :start AND :end " +
                    "ORDER BY checkInTime DESC",
                    AttendanceRecord.class
            );
            query.setParameter("workerId", workerId);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            return query.list();
        } finally {
            session.close();
        }
    }

    /**
     * Count today's check-ins
     *
     * @param date Date to check
     * @return Count of check-ins
     */
    public long countTodayCheckIns(LocalDate date) {
        Session session = sessionFactory.openSession();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(DISTINCT worker.id) FROM AttendanceRecord " +
                    "WHERE checkInTime >= :startOfDay AND checkInTime < :endOfDay",
                    Long.class
            );
            query.setParameter("startOfDay", startOfDay);
            query.setParameter("endOfDay", endOfDay);
            Long result = query.uniqueResult();
            return result != null ? result : 0;
        } finally {
            session.close();
        }
    }

    /**
     * Find attendance record by ID
     *
     * @param id Record ID
     * @return Optional containing record if found
     */
    public Optional<AttendanceRecord> findById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            AttendanceRecord record = session.get(AttendanceRecord.class, id);
            return Optional.ofNullable(record);
        } finally {
            session.close();
        }
    }
}
