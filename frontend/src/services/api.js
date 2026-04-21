import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attendance API endpoints
export const attendanceAPI = {
  checkIn: (employeeId, imagePath, latitude, longitude) => {
    const formData = new FormData();
    formData.append('employeeId', employeeId);
    formData.append('imagePath', imagePath);
    formData.append('latitude', latitude);
    formData.append('longitude', longitude);
    return apiClient.post('/attendance/checkin', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  checkOut: (employeeId, imagePath, latitude, longitude) => {
    const formData = new FormData();
    formData.append('employeeId', employeeId);
    formData.append('imagePath', imagePath);
    formData.append('latitude', latitude);
    formData.append('longitude', longitude);
    return apiClient.post('/attendance/checkout', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  getTodayStatus: () => apiClient.get('/attendance/today'),
  getReport: (startDate, endDate) => apiClient.get('/attendance/report', { params: { startDate, endDate } }),
  getWorkerReport: (workerId, startDate, endDate) => 
    apiClient.get(`/attendance/report/${workerId}`, { params: { startDate, endDate } }),
};

// Workers API endpoints
export const workersAPI = {
  registerWorker: (data) => apiClient.post('/workers/register', data),
  getAllWorkers: () => apiClient.get('/workers'),
  getWorker: (workerId) => apiClient.get(`/workers/${workerId}`),
  getWorkersByRole: (role) => apiClient.get(`/workers/role/${role}`),
  updateWorker: (workerId, data) => apiClient.put(`/workers/${workerId}`, data),
  deactivateWorker: (workerId) => apiClient.put(`/workers/${workerId}/deactivate`, {}),
};

// Waste Management API endpoints
export const wasteAPI = {
  createTask: (data) => apiClient.post('/waste-tasks/create', data),
  getPendingTasks: () => apiClient.get('/waste-tasks/pending'),
  getInProgressTasks: () => apiClient.get('/waste-tasks/in-progress'),
  getWorkerTasks: (workerId) => apiClient.get(`/waste-tasks/worker/${workerId}`),
  assignWorkerToTask: (taskId, workerId) => 
    apiClient.post(`/waste-tasks/${taskId}/assign-worker`, { workerId }),
  assignDriverToTask: (taskId, driverId) => 
    apiClient.post(`/waste-tasks/${taskId}/assign-driver`, { driverId }),
  completeTask: (taskId, data) => apiClient.post(`/waste-tasks/${taskId}/complete`, data),
  cancelTask: (taskId, reason) => apiClient.post(`/waste-tasks/${taskId}/cancel`, { reason }),
};

export default apiClient;
