import React, { useState, useEffect } from 'react';
import { workersAPI } from '../services/api';
import '../styles/Tables.css';

export default function Workers() {
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchWorkers();
  }, []);

  const fetchWorkers = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await workersAPI.getAllWorkers();
      setWorkers(response.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch workers');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="workers-container">
      <h1>Workers Management</h1>

      {error && <div className="error-message">{error}</div>}

      {loading ? (
        <div className="loading">Loading workers...</div>
      ) : workers.length > 0 ? (
        <div className="table-container">
          <table className="report-table">
            <thead>
              <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Department</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {workers.map((worker, idx) => (
                <tr key={idx}>
                  <td>{worker.employeeId}</td>
                  <td>{worker.fullName}</td>
                  <td>{worker.email}</td>
                  <td><span className="badge">{worker.role}</span></td>
                  <td>{worker.department}</td>
                  <td>
                    <span className={`status ${worker.active ? 'active' : 'inactive'}`}>
                      {worker.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="no-data">No workers found.</div>
      )}
    </div>
  );
}
