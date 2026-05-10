import api from './api';

const adminService = {
  getRevenueReport: async () => {
    const response = await api.get('/admin/reports/revenue');
    return response.data;
  },

  getUserActivity: async () => {
    const response = await api.get('/admin/reports/user-activity');
    return response.data;
  },

  getPeakHours: async () => {
    const response = await api.get('/admin/performance/peak-hours');
    return response.data;
  },

  getPeakHoursByStation: async (stationId) => {
    const response = await api.get(`/admin/performance/peak-hours/${stationId}`);
    return response.data;
  },

  getSystemHealth: async () => {
    const response = await api.get('/admin/health');
    return response.data;
  },

  updateStationConfig: async (stationId, pricing, hours) => {
    const response = await api.patch(`/admin/config/station/${stationId}`, null, {
      params: {
        pricingPerKWh: pricing,
        operatingHours: hours
      }
    });
    return response.data;
  },

  getAllStations: async () => {
    const response = await api.get('/stations');
    return response.data;
  },
  
  getAllReservations: async () => {
    const response = await api.get('/admin/reservations');
    return response.data;
  }
};

export default adminService;
