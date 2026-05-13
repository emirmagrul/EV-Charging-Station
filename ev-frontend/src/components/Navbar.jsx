import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import NotificationBell from './notifications/NotificationBell';
import VehicleDropdown from './shared/VehicleDropdown';
import FavoriteDropdown from './shared/FavoriteDropdown';
import './Navbar.css';

const Navbar = () => {
  const { user, isAuthenticated, logout, favorites, toggleFavorite, vehicles, deleteVehicle } = useAuth();

  const { 
    showVehiclesModal, setShowVehiclesModal, 
    showFavoritesModal, setShowFavoritesModal,
    setSelectedStation,
    selectedVehicle, setSelectedVehicle
  } = useUI();

  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="navbar-container">
      <nav className="navbar glass-panel">
        <Link 
          to={isAuthenticated && user ? (
            user.role === 'ADMIN' ? "/admin-dashboard" : 
            user.role === 'OPERATOR' ? "/operator-dashboard" : "/dashboard"
          ) : "/"} 
          className="nav-brand" 
          onClick={() => {setShowVehiclesModal(false); setShowFavoritesModal(false);}}
        >
          ⚡ <span>EV</span> Charger
        </Link>

        <div className="nav-links">
          {isAuthenticated && user ? (
            <>
              {user.role === 'DRIVER' && (
                <>
                  <Link 
                    to="/dashboard" 
                    className="nav-link" 
                    onClick={() => {setShowVehiclesModal(false); setShowFavoritesModal(false);}}
                  >
                    İstasyonlar
                  </Link>
                  <div className="nav-item-dropdown">
                    <button 
                      className={`nav-link-btn ${showVehiclesModal ? 'active' : ''}`} 
                      onClick={() => {setShowVehiclesModal(!showVehiclesModal); setShowFavoritesModal(false);}}
                    >
                      Araçlarım <span className="count-badge">{vehicles.length}</span>
                    </button>
                    {showVehiclesModal && (
                      <VehicleDropdown 
                        vehicles={vehicles}
                        deleteVehicle={deleteVehicle}
                        selectedVehicle={selectedVehicle}
                        setSelectedVehicle={setSelectedVehicle}
                        setShowVehiclesModal={setShowVehiclesModal}
                      />
                    )}
                  </div>

                  <div className="nav-item-dropdown">
                    <button 
                      className={`nav-link-btn ${showFavoritesModal ? 'active' : ''}`} 
                      onClick={() => {setShowFavoritesModal(!showFavoritesModal); setShowVehiclesModal(false);}}
                    >
                      Favoriler <span className="count-badge">{favorites.length}</span>
                    </button>
                    {showFavoritesModal && (
                      <FavoriteDropdown 
                        favorites={favorites}
                        toggleFavorite={toggleFavorite}
                        setSelectedStation={setSelectedStation}
                        setShowFavoritesModal={setShowFavoritesModal}
                      />
                    )}
                  </div>
                </>
              )}

              {user.role === 'ADMIN' && (
                <Link to="/admin-dashboard" className="nav-link">Admin Paneli</Link>
              )}
              {user.role === 'OPERATOR' && (
                <Link to="/operator-dashboard" className="nav-link">Operatör Paneli</Link>
              )}
              <NotificationBell />
              <button className="btn-outline-mini" onClick={handleLogout}>Çıkış</button>
            </>
          ) : (
            <>
              <Link to="/" className="nav-link">Ana Sayfa</Link>
              <Link to="/login" className="btn-primary-new">Giriş Yap</Link>
            </>
          )}
        </div>
      </nav>
    </div>
  );
};

export default Navbar;
