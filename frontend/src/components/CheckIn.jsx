import React, { useState } from 'react';
import axios from 'axios';

export default function CheckIn() {
  const [employeeId, setEmployeeId] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleImageChange = (e) => {
    setImageFile(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const formData = new FormData();
    formData.append('employeeId', employeeId);
    formData.append('imagePath', imageFile);
    formData.append('latitude', latitude);
    formData.append('longitude', longitude);

    try {
      const response = await axios.post('/api/attendance/checkin', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setMessage('Check-in successful!');
      setEmployeeId('');
      setImageFile(null);
    } catch (error) {
      setMessage('Check-in failed: ' + error.response.data.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <h2>Employee Check-In</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Employee ID:</label>
          <input 
            type="text" 
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label>Capture Image:</label>
          <input 
            type="file" 
            accept="image/*"
            onChange={handleImageChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Latitude:</label>
          <input 
            type="number" 
            step="0.0001"
            value={latitude}
            onChange={(e) => setLatitude(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label>Longitude:</label>
          <input 
            type="number" 
            step="0.0001"
            value={longitude}
            onChange={(e) => setLongitude(e.target.value)}
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? 'Processing...' : 'Check In'}
        </button>
      </form>

      {message && <div className="message">{message}</div>}
    </div>
  );
}
