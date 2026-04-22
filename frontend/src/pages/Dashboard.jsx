import React, { useState, useEffect } from 'react';
import { attendanceAPI, wasteAPI } from '../services/api';
import '../styles/Dashboard.css';

export default function Dashboard() {
  const [dashboardData, setDashboardData] = useState({
    presentEmployees: 0,
    absentEmployees: 0,
    attendanceRate: '0.00%',
    pendingTasks: 0,
    inProgressTasks: 0,
    totalWorkers: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError('');

      // First verify backend connectivity to avoid multiple noisy failures.
      const attendanceRes = await attendanceAPI.getTodayStatus();

      const [pendingResult, inProgressResult] = await Promise.allSettled([
        wasteAPI.getPendingTasks(),
        wasteAPI.getInProgressTasks(),
      ]);

      const pendingTasks =
        pendingResult.status === 'fulfilled'
          ? pendingResult.value?.data?.data?.tasks?.length || 0
          : 0;

      const inProgressTasks =
        inProgressResult.status === 'fulfilled'
          ? inProgressResult.value?.data?.data?.tasks?.length || 0
          : 0;

      const partialDataFailed =
        pendingResult.status === 'rejected' || inProgressResult.status === 'rejected';

      setDashboardData({
        presentEmployees: attendanceRes.data?.data?.present || 0,
        absentEmployees: attendanceRes.data?.data?.absent || 0,
        attendanceRate: attendanceRes.data?.data?.attendance_rate || '0.00%',
        pendingTasks,
        inProgressTasks,
        totalWorkers: attendanceRes.data?.data?.total_workers || 0,
      });

      if (partialDataFailed) {
        setError('Some dashboard sections could not be loaded.');
      }
    } catch (err) {
      setError('Cannot connect to backend server. Start backend on http://localhost:8080.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      
      {error && <div className="error-message">{error}</div>}

      <div className="stats-grid">
        <div className="stat-card attendance">
          <div className="stat-icon">👥</div>
          <div className="stat-content">
            <h3>Present Employees</h3>
            <p className="stat-value">{dashboardData.presentEmployees}</p>
          </div>
        </div>

        <div className="stat-card absent">
          <div className="stat-icon">❌</div>
          <div className="stat-content">
            <h3>Absent Employees</h3>
            <p className="stat-value">{dashboardData.absentEmployees}</p>
          </div>
        </div>

        <div className="stat-card rate">
          <div className="stat-icon">📈</div>
          <div className="stat-content">
            <h3>Attendance Rate</h3>
            <p className="stat-value">{dashboardData.attendanceRate}</p>
          </div>
        </div>

        <div className="stat-card pending">
          <div className="stat-icon">⏳</div>
          <div className="stat-content">
            <h3>Pending Tasks</h3>
            <p className="stat-value">{dashboardData.pendingTasks}</p>
          </div>
        </div>

        <div className="stat-card progress">
          <div className="stat-icon">🔄</div>
          <div className="stat-content">
            <h3>In Progress Tasks</h3>
            <p className="stat-value">{dashboardData.inProgressTasks}</p>
          </div>
        </div>

        <div className="stat-card waste">
          <div className="stat-icon">♻️</div>
          <div className="stat-content">
            <h3>Total Workers</h3>
            <p className="stat-value">{dashboardData.totalWorkers}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
