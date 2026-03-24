import axios from 'axios';
import authService from './authService';

const BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = authService.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = authService.getRefreshToken();
        if (refreshToken) {
          const response = await axios.post(`${BASE_URL}/auth/refresh-token`, { refreshToken });
          const { accessToken } = response.data.data;
          authService.setAccessToken(accessToken);
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch {
        authService.logout();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

const apiService = {
  // Auth
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  refreshToken: (refreshToken) => api.post('/auth/refresh-token', { refreshToken }),
  setup2fa: () => api.post('/auth/setup-2fa'),
  verify2fa: (totpCode) => api.post('/auth/verify-2fa', { totpCode }),

  // Files
  uploadFile: (formData, onUploadProgress) =>
    api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    }),
  listFiles: () => api.get('/files'),
  getFile: (id) => api.get(`/files/${id}`),
  downloadFile: (id) =>
    api.get(`/files/${id}/download`, { responseType: 'blob' }),
  deleteFile: (id) => api.delete(`/files/${id}`),
  renameFile: (id, filename) => api.put(`/files/${id}`, { filename }),

  // Shares
  createShare: (data) => api.post('/share/create', data),
  getShareHistory: () => api.get('/share/history'),
  revokeShare: (id) => api.delete(`/share/${id}`),
  accessShare: (token, password) => {
    const params = password ? { password } : {};
    return api.get(`/share/${token}`, { params, responseType: 'blob' });
  },

  // Admin
  getUsers: () => api.get('/admin/users'),
  updateUserRole: (id, role) => api.put(`/admin/users/${id}`, { role }),
  getAnalytics: () => api.get('/admin/analytics'),
  getLogs: (limit = 100) => api.get(`/admin/logs?limit=${limit}`),
  getAlerts: () => api.get('/admin/alerts'),

  // User
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data) => api.put('/user/profile', data),
  getActivity: () => api.get('/user/activity'),
  getStorageUsage: () => api.get('/user/storage'),
};

export default apiService;
