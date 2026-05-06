import React, { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  
  // Data States (Keep these as they are user-specific)
  const [favorites, setFavorites] = useState([]);
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (token && savedUser) {
      setUser(JSON.parse(savedUser));
      setIsAuthenticated(true);
    }
    
    const savedFavs = JSON.parse(localStorage.getItem('favorites') || '[]');
    setFavorites(savedFavs);
  }, []);

  const toggleFavorite = (station) => {
    let updatedFavs;
    if (favorites.some(f => f.id === station.id)) {
      updatedFavs = favorites.filter(f => f.id !== station.id);
    } else {
      updatedFavs = [...favorites, station];
    }
    setFavorites(updatedFavs);
    localStorage.setItem('favorites', JSON.stringify(updatedFavs));
  };

  const login = (userData, token) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    setIsAuthenticated(true);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setIsAuthenticated(false);
  };

  return (
    <AuthContext.Provider value={{ 
      user, isAuthenticated, login, logout,
      favorites, setFavorites, toggleFavorite,
      vehicles, setVehicles
    }}>
      {children}
    </AuthContext.Provider>
  );
};





export const useAuth = () => useContext(AuthContext);
