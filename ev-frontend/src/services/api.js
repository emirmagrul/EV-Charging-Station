import axios from 'axios';

// Backend bağlantısı için merkezi yapılandırma
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Global İstek Günlüğü (Interceptor)
api.interceptors.request.use(config => {
  console.log(`API Request: ${config.method.toUpperCase()} ${config.url}`, config.params || '');
  return config;
});

// Global Hata Yakalayıcı (Interceptor)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.data && error.response.data.error) {
      // Backend'den gelen düzenli hata mesajını fırlat
      return Promise.reject(new Error(error.response.data.error));
    }
    return Promise.reject(error);
  }
);

export default api;

