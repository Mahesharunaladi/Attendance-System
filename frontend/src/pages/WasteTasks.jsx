import React, { useState, useEffect } from 'react';
import { wasteAPI } from '../services/api';
import '../styles/Tables.css';

export default function WasteTasks() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('all'); // all, pending, in-progress

  useEffect(() => {
    fetchTasks();
  }, [filter]);

  const fetchTasks = async () => {
    setLoading(true);
    setError('');

    try {
      let response;
      if (filter === 'pending') {
        response = await wasteAPI.getPendingTasks();
      } else if (filter === 'in-progress') {
        response = await wasteAPI.getInProgressTasks();
      } else {
        response = await wasteAPI.getPendingTasks();
        const inProgressRes = await wasteAPI.getInProgressTasks();
        response.data.data = [...response.data.data, ...inProgressRes.data.data];
      }
      setTasks(response.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch tasks');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="tasks-container">
      <h1>Waste Management Tasks</h1>

      <div className="filter-section">
        <button
          className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
          onClick={() => setFilter('all')}
        >
          All Tasks
        </button>
        <button
          className={`filter-btn ${filter === 'pending' ? 'active' : ''}`}
          onClick={() => setFilter('pending')}
        >
          Pending
        </button>
        <button
          className={`filter-btn ${filter === 'in-progress' ? 'active' : ''}`}
          onClick={() => setFilter('in-progress')}
        >
          In Progress
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading tasks...</div>
      ) : tasks.length > 0 ? (
        <div className="table-container">
          <table className="report-table">
            <thead>
              <tr>
                <th>Task ID</th>
                <th>Area</th>
                <th>Waste Type</th>
                <th>Est. Weight (kg)</th>
                <th>Status</th>
                <th>Assigned Worker</th>
                <th>Driver</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task, idx) => (
                <tr key={idx}>
                  <td>{task.taskId}</td>
                  <td>{task.area}</td>
                  <td>{task.wasteType}</td>
                  <td>{task.estimatedWeight}</td>
                  <td><span className={`status ${task.status.toLowerCase()}`}>{task.status}</span></td>
                  <td>{task.assignedWorkerName || '-'}</td>
                  <td>{task.driverName || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="no-data">No tasks found.</div>
      )}
    </div>
  );
}
