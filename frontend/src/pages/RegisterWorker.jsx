import React, { useState } from 'react';
import LiveCameraCapture from '../components/LiveCameraCapture';
import { workersAPI } from '../services/api';
import '../styles/Forms.css';
import '../styles/Dashboard.css';

export default function RegisterWorker() {
  const [registration, setRegistration] = useState({
    fullName: '',
    phoneNumber: '',
    aadharNumber: '',
    employeeId: '',
    role: 'CLEANER',
    imageFile: null,
  });
  const [registering, setRegistering] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');

  const handleChange = (event) => {
    const { name, value } = event.target;
    setRegistration((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setRegistering(true);
    setMessage('');

    try {
      const formData = new FormData();
      formData.append('fullName', registration.fullName);
      formData.append('phoneNumber', registration.phoneNumber);
      formData.append('aadharNumber', registration.aadharNumber);
      formData.append('role', registration.role);
      if (registration.employeeId.trim()) {
        formData.append('employeeId', registration.employeeId.trim());
      }
      if (registration.imageFile) {
        formData.append('image', registration.imageFile);
      }

      const response = await workersAPI.registerWorker(formData);
      const saved = response.data?.data || {};

      setMessageType('success');
      setMessage(`Worker registered successfully. Employee ID: ${saved.employee_id || 'Generated automatically'}`);
      setRegistration({
        fullName: '',
        phoneNumber: '',
        aadharNumber: '',
        employeeId: '',
        role: 'CLEANER',
        imageFile: null,
      });
    } catch (error) {
      setMessageType('error');
      setMessage(error.response?.data?.message || 'Failed to register worker');
    } finally {
      setRegistering(false);
    }
  };

  return (
    <div className="dashboard">
      <div className="registration-copy">
        <span className="dashboard-eyebrow">Registration</span>
        <h1>Live Worker Registration</h1>
        <p>
          Capture a live worker image here, then save their name, phone number,
          Aadhar number, employee ID, and role from the same portal.
        </p>
      </div>

      <form className="registration-card" onSubmit={handleSubmit}>
        <div className="registration-camera">
          <label>Live Photo *</label>
          <LiveCameraCapture
            mode="register"
            imageFile={registration.imageFile}
            onCapture={(imageFile) =>
              setRegistration((prev) => ({ ...prev, imageFile }))
            }
          />
        </div>

        <div className="registration-fields">
          <div className="registration-grid">
            <div className="form-group">
              <label htmlFor="fullName">Full Name *</label>
              <input
                id="fullName"
                name="fullName"
                value={registration.fullName}
                onChange={handleChange}
                placeholder="Enter worker name"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="phoneNumber">Phone Number *</label>
              <input
                id="phoneNumber"
                name="phoneNumber"
                value={registration.phoneNumber}
                onChange={handleChange}
                placeholder="10-digit phone number"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="aadharNumber">Aadhar Number *</label>
              <input
                id="aadharNumber"
                name="aadharNumber"
                value={registration.aadharNumber}
                onChange={handleChange}
                placeholder="12-digit Aadhar number"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="employeeId">Employee ID</label>
              <input
                id="employeeId"
                name="employeeId"
                value={registration.employeeId}
                onChange={handleChange}
                placeholder="Leave blank to auto-generate"
              />
            </div>

            <div className="form-group">
              <label htmlFor="role">Role</label>
              <select
                id="role"
                name="role"
                value={registration.role}
                onChange={handleChange}
              >
                <option value="CLEANER">Cleaner</option>
                <option value="DRIVER">Driver</option>
                <option value="HELPER">Helper</option>
                <option value="SUPERVISOR">Supervisor</option>
                <option value="MANAGER">Manager</option>
              </select>
            </div>
          </div>

          <button
            type="submit"
            className="submit-btn"
            disabled={
              registering ||
              !registration.imageFile ||
              !registration.fullName ||
              !registration.phoneNumber ||
              !registration.aadharNumber
            }
          >
            {registering ? 'Registering worker...' : 'Register Worker'}
          </button>

          {message && (
            <div className={`message ${messageType}`}>
              {message}
            </div>
          )}
        </div>
      </form>
    </div>
  );
}
