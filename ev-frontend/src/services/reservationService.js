import api from './api';

const reservationService = {
  makeReservation: async (reservationDto) => {
    const response = await api.post('/reservations/make', reservationDto);
    return response.data;
  },

  confirmReservation: async (id) => {
    const response = await api.post(`/reservations/${id}/confirm`);
    return response.data;
  },

  cancelReservation: async (id) => {
    const response = await api.post(`/reservations/${id}/cancel`);
    return response.data;
  },

  getMyReservations: async (driverId) => {
    const response = await api.get(`/reservations/driver/${driverId}`);
    return response.data;
  },

  getBookedSlots: async (chargerId, date) => {
    const response = await api.get(`/reservations/charger/${chargerId}/booked-slots`, {
      params: { date }
    });
    return response.data;
  }
};

export default reservationService;
