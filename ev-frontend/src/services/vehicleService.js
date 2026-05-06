import api from './api';

const vehicleService = {
  registerVehicle: async (vehicleData) => {
    const response = await api.post('/vehicles/register', vehicleData);
    return response.data;
  },

  getDriverVehicles: async (driverId) => {
    const response = await api.get(`/vehicles/driver/${driverId}`);
    return response.data;
  },

  deleteVehicle: async (id) => {
    const response = await api.delete(`/vehicles/${id}`);
    return response.data;
  },


  getAllConnectorTypes: async () => {
    const response = await api.get('/connector-types');
    return response.data;
  },

  registerConnectorType: async (data) => {
    const response = await api.post('/connector-types', data);
    return response.data;
  }
};


export default vehicleService;
