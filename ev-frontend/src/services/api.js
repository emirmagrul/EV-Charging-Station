import axios from 'axios';

// Backend baglantisi icin merkezi yapilandirma
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Global Istek Gunlugu (Interceptor) - sadece development'ta
api.interceptors.request.use(config => {
  if (import.meta.env.DEV) {
    console.log(`API Request: ${config.method.toUpperCase()} ${config.url}`, config.params || '');
  }
  return config;
});

// Global Hata Yakalayici (Interceptor)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.data && error.response.data.error) {
      // Backend'den gelen duzenli hata mesajini firllat
      return Promise.reject(new Error(error.response.data.error));
    }
    return Promise.reject(error);
  }
);

export default api;
