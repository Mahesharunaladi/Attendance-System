import React, { useState } from 'react';
import { attendanceAPI } from '../services/api';
import '../styles/Forms.css';

export default function CheckOut() {
  const [formData, setFormData] = useState({
    employeeId: '',
    latitude: '',
    longitude: '',
    imageFile: null,
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e) => {
    setFormData(prev => ({ ...prev, imageFile: e.target.files[0] }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      await attendanceAPI.checkOut(
        formData.employeeId,
        formData.imageFile,
        formData.latitude,
        formData.longitude
      );
      setMessageType('success');
      setMessage('✓ Check-out successful!');
      setFormData({ employeeId: '', latitude: '', longitude: '', imageFile: null });
    } catch (error) {
      setMessageType('error');
      setMessage('✗ ' + (error.response?.data?.message || 'Check-out failed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container">
      <div className="form-card">
        <h2>Employee Check-Out</h2>
        <p className="form-description">Use face recognition to check out</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="employeeId">Employee ID *</label>
            <input
              id="employeeId"
              type="text"
              name="employeeId"
              value={formData.employeeId}
              onChange={handleInputChange}
              placeholder="Enter your employee ID"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="imageFile">Capture Image *</label>
            <input
              id="imageFile"
              type="file"
              name="imageFile"
              accept="image/*"
              onChange={handleFileChange}
              required
            />
            {formData.imageFile && <p className="file-name">{formData.imageFile.name}</p>}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="latitude">Latitude *</label>
              <input
                id="latitude"
                type="number"
                name="latitude"
                step="0.0001"
                value={formData.latitude}
                onChange={handleInputChange}
                placeholder="-90 to 90"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="longitude">Longitude *</label>
              <input
                id="longitude"
                type="number"
                name="longitude"
                step="0.0001"
                value={formData.longitude}
                onChange={handleInputChange}
                placeholder="-180 to 180"
                required
              />
            </div>
          </div>

          <button type="submit" disabled={loading} className="submit-btn">
            {loading ? 'Processing...' : 'Check Out'}
          </button>
        </form>

        {message && (
          <div className={`message ${messageType}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
}
