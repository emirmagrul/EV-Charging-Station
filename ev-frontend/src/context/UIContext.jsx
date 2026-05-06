import React, { createContext, useContext, useState } from 'react';

const UIContext = createContext();

export const UIProvider = ({ children }) => {
  const [showVehiclesModal, setShowVehiclesModal] = useState(false);
  const [showFavoritesModal, setShowFavoritesModal] = useState(false);
  const [selectedStation, setSelectedStation] = useState(null);

  const closeAllModals = () => {
    setShowVehiclesModal(false);
    setShowFavoritesModal(false);
  };

  return (
    <UIContext.Provider value={{ 
      showVehiclesModal, setShowVehiclesModal,
      showFavoritesModal, setShowFavoritesModal,
      selectedStation, setSelectedStation,
      closeAllModals
    }}>
      {children}
    </UIContext.Provider>
  );
};

export const useUI = () => useContext(UIContext);
