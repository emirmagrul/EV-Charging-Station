import api from './api';

const operatorService = {
  // ─── İstasyon & Ünite Yönetimi ───────────────────────────────────────────

  // Tüm istasyonları getirir
  getAllStations: async () => {
    const response = await api.get('/stations');
    return response.data;
  },

  // Operatöre ait istasyonları getirir
  getStationsByOperator: async (operatorId) => {
    const response = await api.get(`/stations/operator/${operatorId}`);
    return response.data;
  },

  // Bir istasyona ait tüm şarj ünitelerini getirir
  getChargersByStation: async (stationId) => {
    const response = await api.get(`/chargers/station/${stationId}`);
    return response.data;
  },

  // Bir şarj ünitesinin durumunu günceller (AVAILABLE, CHARGING, OUT_OF_SERVICE, OFFLINE)
  updateChargerStatus: async (chargerId, status) => {
    const response = await api.patch(`/chargers/${chargerId}/status`, null, {
      params: { status }
    });
    return response.data;
  },

  // Arıza raporu oluşturur
  createFaultReport: async (reportData) => {
    const response = await api.post('/fault-reports/report', reportData);
    return response.data;
  },

  // ─── Arıza Raporu Yönetimi ────────────────────────────────────────────────

  // Tüm arıza raporlarını getirir
  getAllFaultReports: async () => {
    const response = await api.get('/fault-reports');
    return response.data;
  },

  // Belirli bir arıza raporunu getirir
  getFaultReportById: async (reportId) => {
    const response = await api.get(`/fault-reports/${reportId}`);
    return response.data;
  },

  // Arıza raporu durumunu günceller (OPEN, IN_PROGRESS, RESOLVED, DISMISSED)
  updateFaultReportStatus: async (reportId, status) => {
    const response = await api.patch(`/fault-reports/${reportId}/status`, null, {
      params: { status }
    });
    return response.data;
  },

  // Bir şarj ünitesine ait arıza raporlarını getirir
  getFaultReportsByCharger: async (chargerId) => {
    const response = await api.get(`/fault-reports/charger/${chargerId}`);
    return response.data;
  },

  // ─── Rezervasyon Yönetimi (Operatör Görünümü) ─────────────────────────────

  // Tüm aktif rezervasyonları getirir
  getAllReservations: async () => {
    const response = await api.get('/reservations');
    return response.data;
  },

  // Belirli bir şarj ünitesinin rezervasyonlarını getirir
  getReservationsByCharger: async (chargerId) => {
    const response = await api.get(`/reservations/charger/${chargerId}`);
    return response.data;
  },

  // Operatör tarafından rezervasyon iptali (bakım/arıza gerekçesiyle)
  cancelReservationByOperator: async (reservationId, reason) => {
    // Backend'de henüz reason parametresi tam desteklenmiyor olabilir, mevcut iptal endpoint'ini kullanıyoruz
    const response = await api.post(`/reservations/${reservationId}/cancel`, null, {
      params: { reason }
    });
    return response.data;
  },
};

export default operatorService;
