import React, { useState } from 'react';
import { attendanceAPI } from '../services/api';
import '../styles/Tables.css';

export default function AttendanceReport() {
  const [dateRange, setDateRange] = useState({ startDate: '', endDate: '' });
  const [reportData, setReportData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleDateChange = (e) => {
    const { name, value } = e.target;
    setDateRange(prev => ({ ...prev, [name]: value }));
  };

  const handleFetch = async () => {
    if (!dateRange.startDate || !dateRange.endDate) {
      setError('Please select both start and end dates');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await attendanceAPI.getReport(dateRange.startDate, dateRange.endDate);
      setReportData(response.data?.data?.records || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="report-container">
      <h1>Attendance Report</h1>

      <div className="filter-section">
        <div className="filter-group">
          <label>Start Date:</label>
          <input
            type="date"
            name="startDate"
            value={dateRange.startDate}
            onChange={handleDateChange}
          />
        </div>

        <div className="filter-group">
          <label>End Date:</label>
          <input
            type="date"
            name="endDate"
            value={dateRange.endDate}
            onChange={handleDateChange}
          />
        </div>

        <button onClick={handleFetch} disabled={loading} className="fetch-btn">
          {loading ? 'Loading...' : 'Fetch Report'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {reportData.length > 0 && (
        <div className="table-container">
          <table className="report-table">
            <thead>
              <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Status</th>
                <th>Check-In</th>
                <th>Check-Out</th>
                <th>Face Match %</th>
              </tr>
            </thead>
            <tbody>
              {reportData.map((record, idx) => (
                <tr key={idx}>
                  <td>{record.employeeId}</td>
                  <td>{record.workerName}</td>
                  <td><span className={`status ${record.status.toLowerCase()}`}>{record.status}</span></td>
                  <td>{record.checkInTime ? new Date(record.checkInTime).toLocaleString() : '-'}</td>
                  <td>{record.checkOutTime ? new Date(record.checkOutTime).toLocaleString() : '-'}</td>
                  <td>{(record.faceMatchConfidence * 100).toFixed(2)}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!loading && reportData.length === 0 && dateRange.startDate && (
        <div className="no-data">No attendance records found for the selected date range.</div>
      )}
    </div>
  );
}
