import api from './api';

const notificationService = {
  getNotifications: async (driverId) => {
    const response = await api.get(`/notifications/driver/${driverId}`);
    return response.data;
  },

  getOperatorNotifications: async (operatorId) => {
    const response = await api.get(`/notifications/operator/${operatorId}`);
    return response.data;
  },

  getUnreadCount: async (driverId) => {
    const response = await api.get(`/notifications/driver/${driverId}/unread-count`);
    return response.data;
  },

  getOperatorUnreadCount: async (operatorId) => {
    const response = await api.get(`/notifications/operator/${operatorId}/unread-count`);
    return response.data;
  },

  markAsRead: async (id) => {
    const response = await api.patch(`/notifications/${id}/read`);
    return response.data;
  },

  markAllAsRead: async (driverId) => {
    const response = await api.post(`/notifications/driver/${driverId}/mark-all-read`);
    return response.data;
  },

  markAllOperatorAsRead: async (operatorId) => {
    const response = await api.post(`/notifications/operator/${operatorId}/mark-all-read`);
    return response.data;
  },
};

export default notificationService;
