import { useEffect, useState } from 'react';
import { attendanceAPI } from '../services/api';
import LiveCameraCapture from '../components/LiveCameraCapture';
import { getCurrentLocation } from '../utils/location';
import '../styles/Forms.css';

export default function CheckOut() {
  const [formData, setFormData] = useState({
    employeeId: '',
    imageFile: null,
  });
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);
  const [location, setLocation] = useState(null);
  const [locationError, setLocationError] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const loadLocation = async () => {
    setLocating(true);
    setLocationError('');

    try {
      const coords = await getCurrentLocation();
      setLocation(coords);
      return coords;
    } catch (error) {
      setLocation(null);
      setLocationError(error.message);
      throw error;
    } finally {
      setLocating(false);
    }
  };

  useEffect(() => {
    loadLocation().catch(() => {});
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      const coords = await loadLocation();
      await attendanceAPI.checkOut(
        formData.employeeId,
        formData.imageFile,
        coords.latitude,
        coords.longitude
      );
      setMessageType('success');
      setMessage('✓ Check-out successful!');
      setFormData({ employeeId: '', imageFile: null });
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
            <LiveCameraCapture
              imageFile={formData.imageFile}
              onCapture={(imageFile) =>
                setFormData(prev => ({ ...prev, imageFile }))
              }
            />
          </div>

          <div className="location-status">
            <span className={location ? 'location-badge success' : 'location-badge pending'}>
              {locating
                ? 'Detecting current location...'
                : location
                  ? `Location captured automatically: ${location.latitude}, ${location.longitude}`
                  : 'Location will be captured automatically'}
            </span>
            <button
              type="button"
              className="location-refresh-btn"
              onClick={() => loadLocation().catch(() => {})}
              disabled={locating || loading}
            >
              {locating ? 'Detecting...' : 'Refresh Location'}
            </button>
          </div>

          {locationError && <p className="camera-error">{locationError}</p>}

          <button
            type="submit"
            disabled={loading || locating || !formData.imageFile || !location}
            className="submit-btn"
          >
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
