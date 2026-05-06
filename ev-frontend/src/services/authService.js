import api from './api';

const authService = {
  login: async (email, password) => {
    const response = await api.post('/drivers/login', { email, password });
    return response.data;
  },

  register: async (userData) => {
    const response = await api.post('/drivers/register', userData);
    return response.data;
  }
};

export default authService;
