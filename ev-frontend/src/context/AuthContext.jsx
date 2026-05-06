import React, { createContext, useState, useContext, useEffect } from 'react';
import vehicleService from '../services/vehicleService';
import api from '../services/api'; // Favorileri çekmek için direkt API kullanabiliriz veya bir servis oluşturabiliriz

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  
  const [favorites, setFavorites] = useState([]);
  const [vehicles, setVehicles] = useState([]);

  // Kullanıcıya özel verileri yükle
  const loadUserData = async (userId) => {
    try {
      // Araçları getir
      const vehicleData = await vehicleService.getDriverVehicles(userId);
      setVehicles(vehicleData);

      // Favorileri backend'den getir
      const favResponse = await api.get(`/drivers/${userId}/favorites`);
      setFavorites(favResponse.data);
    } catch (err) {
      console.error("Kullanıcı verileri yüklenemedi:", err);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (token && savedUser) {
      const parsedUser = JSON.parse(savedUser);
      setUser(parsedUser);
      setIsAuthenticated(true);
      loadUserData(parsedUser.id);
    }
  }, []);

  const login = (userData, token) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    setIsAuthenticated(true);
    
    // Login sonrası sadece o kullanıcıya ait verileri yükle
    loadUserData(userData.id);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setIsAuthenticated(false);
    setVehicles([]);
    setFavorites([]); // Favorileri temizle!
  };

  const deleteVehicle = async (vehicleId) => {
    try {
      await vehicleService.deleteVehicle(vehicleId);
      setVehicles(vehicles.filter(v => v.id !== vehicleId));
      return true;
    } catch (err) {
      console.error("Araç silinemedi:", err);
      return false;
    }
  };

  const addBalance = async (amount) => {
    if (!user) return false;
    try {
      await api.post(`/drivers/${user.id}/balance`, null, { params: { amount } });
      // Güncel bakiyeyi almak için profili tekrar çekebilir veya locali güncelleyebiliriz
      const profileRes = await api.get(`/drivers/${user.id}`);
      const updatedUser = profileRes.data;
      setUser(updatedUser);
      localStorage.setItem('user', JSON.stringify(updatedUser));
      return true;
    } catch (err) {
      console.error("Bakiye yüklenemedi:", err);
      return false;
    }
  };

  const refreshUser = async () => {
    if (!user) return;
    try {
      const profileRes = await api.get(`/drivers/${user.id}`);
      const updatedUser = profileRes.data;
      setUser(updatedUser);
      localStorage.setItem('user', JSON.stringify(updatedUser));
    } catch (err) {
      console.error("Kullanıcı güncellenemedi:", err);
    }
  };




  const toggleFavorite = async (station) => {
    if (!user) return;

    const isFavorite = favorites.some(f => f.id === station.id);
    try {
      if (isFavorite) {
        await api.delete(`/drivers/${user.id}/favorites/${station.id}`);
        setFavorites(favorites.filter(f => f.id !== station.id));
      } else {
        await api.post(`/drivers/${user.id}/favorites/${station.id}`);
        setFavorites([...favorites, station]);
      }
    } catch (err) {
      console.error("Favori işlemi başarısız:", err);
    }
  };

  return (
    <AuthContext.Provider value={{ 
      user, isAuthenticated, login, logout,
      favorites, setFavorites, toggleFavorite,
      vehicles, setVehicles, deleteVehicle, addBalance, refreshUser
    }}>



      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
