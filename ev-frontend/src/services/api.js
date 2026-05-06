import axios from 'axios';

// Backend bağlantısı için merkezi yapılandırma
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

export default api;
