import api from './api';

const mapService = {
  // Kullanıcının konumuna göre istasyonları (mesafe sıralı) getirir
  getNearbyStations: async (userLat, userLng, connectorType = null) => {
    const params = { userLat, userLng };
    if (connectorType) params.connectorType = connectorType;
    
    const response = await api.get('/map/stations', { params });
    return response.data;
  },

  // Seçilen istasyon için Google Maps navigasyon linkini oluşturur
  getNavigationUrl: async (stationId, userLat, userLng) => {
    const response = await api.get(`/map/navigation/${stationId}`, {
      params: { userLat, userLng }
    });
    return response.data;
  }
};

export default mapService;
