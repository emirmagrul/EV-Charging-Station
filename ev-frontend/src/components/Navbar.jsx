import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import './Navbar.css';

const Navbar = () => {
  const { isAuthenticated, logout, favorites, toggleFavorite, vehicles } = useAuth();
  const { 
    showVehiclesModal, setShowVehiclesModal, 
    showFavoritesModal, setShowFavoritesModal,
    setSelectedStation 
  } = useUI();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="navbar-container">
      <nav className="navbar glass-panel">
        <Link to={isAuthenticated ? "/dashboard" : "/"} className="nav-brand" onClick={() => {setShowVehiclesModal(false); setShowFavoritesModal(false);}}>
          ⚡ <span>EV</span> Charger
        </Link>

        <div className="nav-links">
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className="nav-link" onClick={() => {setShowVehiclesModal(false); setShowFavoritesModal(false);}}>İstasyonlar</Link>
              
              <div className="nav-item-dropdown">
                <button 
                  className={`nav-link-btn ${showVehiclesModal ? 'active' : ''}`} 
                  onClick={() => {setShowVehiclesModal(!showVehiclesModal); setShowFavoritesModal(false);}}
                >
                  Araçlarım <span className="count-badge">{vehicles.length}</span>
                </button>
                {showVehiclesModal && (
                  <div className="dropdown-panel glass-panel">
                    <div className="dropdown-header">
                      <span>Kayıtlı Araçlarım</span>
                    </div>
                    <div className="dropdown-body">
                      {vehicles.length > 0 ? (
                        vehicles.map(v => (
                          <div key={v.id} className="dropdown-item">
                            <div className="item-icon">🚗</div>
                            <div className="item-info">
                              <h4>{v.brand} {v.model}</h4>
                              <p>{v.plateNumber}</p>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="empty-dropdown">Henüz araç eklemediniz.</div>
                      )}
                      <button className="btn-primary-new btn-full" onClick={() => navigate('/vehicles/add')}>+ Araç Ekle</button>
                    </div>
                  </div>
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
                  <div className="dropdown-panel glass-panel">
                    <div className="dropdown-header">
                      <span>Favori İstasyonlarım</span>
                    </div>
                    <div className="dropdown-body">
                      {favorites.length > 0 ? (
                        favorites.map(st => (
                          <div 
                            key={st.id} 
                            className="dropdown-item clickable" 
                            onClick={() => {
                              setSelectedStation(st);
                              setShowFavoritesModal(false);
                            }}
                          >
                            <div className="item-info">
                              <h4>{st.stationName}</h4>
                              <p>{st.address.substring(0, 30)}...</p>
                            </div>
                            <button className="remove-btn-mini" onClick={(e) => {e.stopPropagation(); toggleFavorite(st);}}>✕</button>
                          </div>
                        ))
                      ) : (
                        <div className="empty-dropdown">Henüz favori istasyonun yok.</div>
                      )}
                    </div>
                  </div>
                )}
              </div>

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
