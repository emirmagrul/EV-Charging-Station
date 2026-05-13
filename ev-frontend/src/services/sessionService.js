import api from './api';

const sessionService = {
  startSession: async (reservationId) => {
    const response = await api.post(`/sessions/start/${reservationId}`);
    return response.data;
  },

  endSession: async (sessionId) => {
    const response = await api.post(`/sessions/${sessionId}/end`);
    return response.data;
  },

  getActiveSession: async (driverId) => {
    try {
      const response = await api.get(`/sessions/active/${driverId}`);
      if (response.status === 204) {
        return null;
      }
      return response.data;
    } catch (error) {
      if (error.response && error.response.status === 204) {
        return null;
      }
      throw error;
    }
  }
};

export default sessionService;
