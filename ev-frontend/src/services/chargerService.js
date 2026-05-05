import api from './api';

const chargerService = {
  // Tüm şarj istasyonlarını (katalog) getirir
  getAllStations: async () => {
    const response = await api.get('/stations');
    return response.data;
  },

  // Belirli bir istasyonun detaylarını getirir
  getStationById: async (id) => {
    const response = await api.get(`/stations/${id}`);
    return response.data;
  },

  // Bir istasyona bağlı olan tüm şarj ünitelerini getirir
  getChargersByStation: async (stationId) => {
    const response = await api.get(`/chargers/station/${stationId}`);
    return response.data;
  },

  // Bir şarj ünitesinin durumunu (Müsait, Şarjda vb.) günceller
  updateChargerStatus: async (id, status) => {
    const response = await api.patch(`/chargers/${id}/status`, null, {
      params: { status }
    });
    return response.data;
  }
};

export default chargerService;
