import { useEffect, useState } from 'react';
import { attendanceAPI } from '../services/api';
import LiveCameraCapture from '../components/LiveCameraCapture';
import { getCurrentLocation } from '../utils/location';
import '../styles/Forms.css';

export default function CheckIn() {
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
  const [detectedWorker, setDetectedWorker] = useState(null);
  const [todayStatus, setTodayStatus] = useState(null);

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
    // Fetch today's attendance status on component mount
    fetchTodayStatus();
  }, []);

  const fetchTodayStatus = async () => {
    try {
      const response = await attendanceAPI.getTodayStatus();
      setTodayStatus(response?.data);
    } catch (error) {
      console.error('Error fetching today status:', error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      const coords = await loadLocation();
      await attendanceAPI.checkIn(
        formData.employeeId,
        formData.imageFile,
        coords.latitude,
        coords.longitude
      );
      setMessageType('success');
      setMessage('✓ Check-in successful!');
      setFormData({ employeeId: '', imageFile: null });
      setDetectedWorker(null);
      
      // Fetch updated status
      setTimeout(() => {
        fetchTodayStatus();
      }, 500);
      
      // Reset button color and message after 4 seconds
      setTimeout(() => {
        setMessageType('');
        setMessage('');
      }, 4000);
    } catch (error) {
      setMessageType('error');
      setMessage('✗ ' + (error.response?.data?.message || 'Check-in failed'));
      
      // Reset error button after 4 seconds
      setTimeout(() => {
        setMessageType('');
        setMessage('');
      }, 4000);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container">
      <div className={`form-card ${detectedWorker ? 'form-card-wide' : ''}`}>
        <h2>Employee Check-In</h2>
        <p className="form-description">Use face recognition to check in</p>

        {todayStatus && (
          <div className={`status-badge ${todayStatus.checkedIn ? 'status-checked-in' : 'status-not-checked'}`}>
            <span className="status-icon">{todayStatus.checkedIn ? '✓' : '○'}</span>
            <span className="status-text">
              {todayStatus.checkedIn 
                ? `✓ Checked In at ${new Date(todayStatus.checkInTime).toLocaleTimeString()}` 
                : 'Not Checked In Today'}
            </span>
          </div>
        )}

        <div className={`checkin-capture-grid ${detectedWorker ? 'has-details' : ''}`}>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="employeeId">Employee ID *</label>
              <input
                id="employeeId"
                type="text"
                name="employeeId"
                value={formData.employeeId}
                onChange={handleInputChange}
                placeholder="Recognized employee ID will appear here"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="imageFile">Capture Image *</label>
              <LiveCameraCapture
                imageFile={formData.imageFile}
                showDetectedWorkerDetails={false}
                onCapture={(imageFile) =>
                  setFormData(prev => ({ ...prev, imageFile }))
                }
                onWorkerDetected={(worker) => {
                  setDetectedWorker(worker);
                  setFormData(prev => ({
                    ...prev,
                    employeeId: worker.employeeId || '',
                  }));
                }}
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
              disabled={loading || locating || !formData.imageFile || !location || !formData.employeeId}
              className={`submit-btn ${messageType === 'success' ? 'btn-success' : messageType === 'error' ? 'btn-error' : ''}`}
            >
              {loading ? 'Processing...' : messageType === 'success' ? '✓ Checked In' : 'Check In'}
            </button>
          </form>

          {detectedWorker && (
            <aside className="detected-worker-panel">
              <h3>Recognized Worker</h3>
              <div className="details-grid">
                <div className="detail-item">
                  <label>Name:</label>
                  <span>{detectedWorker.fullName}</span>
                </div>
                <div className="detail-item">
                  <label>Employee ID:</label>
                  <span>{detectedWorker.employeeId}</span>
                </div>
                <div className="detail-item">
                  <label>Phone Number:</label>
                  <span>{detectedWorker.phoneNumber || 'N/A'}</span>
                </div>
                <div className="detail-item">
                  <label>Aadhar Number:</label>
                  <span>{detectedWorker.aadharNumber || 'N/A'}</span>
                </div>
                <div className="detail-item">
                  <label>Role:</label>
                  <span>{detectedWorker.role || 'N/A'}</span>
                </div>
                <div className="detail-item">
                  <label>Department:</label>
                  <span>{detectedWorker.department || 'N/A'}</span>
                </div>
              </div>
            </aside>
          )}
        </div>

        {message && (
          <div className={`message ${messageType}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
}
