import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import { useGeolocation } from '../hooks/useGeolocation';
import { useStations } from '../hooks/useStations';
import { userIcon, getPinIcon } from '../components/map/MapIcons';
import { RoutingMachine, ChangeView } from '../components/map/MapLayers';
import StationCard from '../components/dashboard/StationCard';
import './Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const { user, favorites, toggleFavorite } = useAuth();
  const { 
    selectedStation, 
    setShowFavoritesModal 
  } = useUI();
  
  const { stations, loading: stationsLoading } = useStations();
  const { coords: userCoords } = useGeolocation();

  const [searchTerm, setSearchTerm] = useState('');
  const [viewMode, setViewMode] = useState('map');
  const [targetRoute, setTargetRoute] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
  const [mapCenter, setMapCenter] = useState(null);
  const [activeStation, setActiveStation] = useState(null);

  useEffect(() => {
    if (userCoords) {
      setMapCenter([userCoords.latitude, userCoords.longitude]);
    }
  }, [userCoords]);

  useEffect(() => {
    if (selectedStation) {
      goToStation(selectedStation);
    }
  }, [selectedStation]);

  const filteredStations = stations.filter(st => 
    st.stationName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    st.address?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const recommendedStations = stations.slice(0, 4);

  const startInternalRouting = (st) => {
    if (!userCoords) return alert("Konum izni gerekiyor.");
    setTargetRoute({ latitude: st.latitude, longitude: st.longitude });
    setViewMode('map');
    setMapCenter([st.latitude, st.longitude]);
    setActiveStation(null);
  };

  const handleReservation = (stId) => {
    navigate(`/reserve/${stId}`);
  };

  const goToStation = (st) => {
    setTargetRoute(null);
    setRouteInfo(null);
    setViewMode('map');
    setMapCenter([st.latitude, st.longitude]);
    setActiveStation(st);
  };

  return (
    <div className="dashboard-container">
      {/* Üst Panel */}
      <header className="dashboard-header">
        <div className="welcome-text">
          <h1>Merhaba, <span className="text-gradient">{user?.name || 'Sürücü'}</span></h1>
          <p>Yolculuğun için enerji dolu bir gün dileriz.</p>
        </div>
        <div className="user-stats glass-panel">
          <div className="stat-item">
            <span className="stat-label">Cüzdan</span>
            <span className="stat-value">0.00 TL</span>
          </div>
          <div className="stat-divider"></div>
          <div className="stat-item">
            <span className="stat-label">Aktif Rezervasyon</span>
            <span className="stat-value">Yok</span>
          </div>
        </div>
      </header>

      {/* Önerilen İstasyonlar */}
      <section className="quick-access-section">
        <div className="section-title-row">
          <h2>Önerilen İstasyonlar</h2>
        </div>
        <div className="horizontal-scroll">
          {recommendedStations.map(st => (
            <div key={st.id} className="recommend-card glass-panel" onClick={() => goToStation(st)}>
              <div className="rec-badge">Önerilen</div>
              <div className="rec-info">
                <h3>{st.stationName}</h3>
                <p>{st.address.substring(0, 30)}...</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Favori İstasyonlar Bölümü */}
      {favorites.length > 0 && (
        <section className="quick-access-section">
          <div className="section-title-row">
            <h2>Favori İstasyonlarım</h2>
            <button className="btn-text" onClick={() => setShowFavoritesModal(true)}>Düzenle</button>
          </div>
          <div className="favorites-scroll">
            {favorites.map(st => (
              <div key={st.id} className="fav-station-card glass-panel" onClick={() => goToStation(st)}>
                <div className="fav-icon">❤️</div>
                <div className="fav-info">
                  <h3>{st.stationName}</h3>
                  <p>{st.address.substring(0, 30)}...</p>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Ana İstasyon Alanı */}
      <section className="stations-main-section">
        <div className="section-header-flex">
          <div className="title-group">
            <h2>İstasyon Bul</h2>
            <p>Konumuna en yakın ve aracına uygun istasyonlar.</p>
          </div>
          <div className="controls-wrapper">
             <div className="search-box-modern">
                <span className="search-icon-inside">🔍</span>
                <input 
                  type="text" 
                  placeholder="İstasyon veya şehir ara..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            <div className="view-toggle glass-panel">
              <button className={viewMode === 'list' ? 'active' : ''} onClick={() => setViewMode('list')}>Liste</button>
              <button className={viewMode === 'map' ? 'active' : ''} onClick={() => setViewMode('map')}>Harita</button>
            </div>
          </div>
        </div>

        {viewMode === 'list' ? (
          <div className="station-grid">
            {filteredStations.map(station => (
              <StationCard 
                key={station.id}
                station={station}
                isFavorite={favorites.some(f => f.id === station.id)}
                onToggleFavorite={toggleFavorite}
                onReserve={handleReservation}
                onDetail={(id) => navigate(`/stations/${id}`)}
                onRoute={startInternalRouting}
              />
            ))}
          </div>
        ) : (
          <div className="map-wrapper glass-panel" style={{position: 'relative'}}>
            {routeInfo && (
              <div className="route-info-overlay glass-panel">
                <div className="info-item">
                  <span className="label">Mesafe</span>
                  <span className="value">{routeInfo.distance} km</span>
                </div>
                <div className="info-divider"></div>
                <div className="info-item">
                  <span className="label">Tahmini Süre</span>
                  <span className="value">{routeInfo.time} dk</span>
                </div>
                <button className="close-route" onClick={() => {setTargetRoute(null); setRouteInfo(null);}}>✕</button>
              </div>
            )}

            <MapContainer 
              center={mapCenter || [38.4237, 27.1428]} 
              zoom={13} 
              style={{ height: '600px', width: '100%' }}
            >
              <ChangeView center={mapCenter} zoom={15} />
              <TileLayer url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" />
              {targetRoute && userCoords && (
                <RoutingMachine userCoords={userCoords} targetCoords={targetRoute} setRouteInfo={setRouteInfo} />
              )}
              {userCoords && (
                <Marker position={[userCoords.latitude, userCoords.longitude]} icon={userIcon}>
                  <Popup>Buradasınız</Popup>
                </Marker>
              )}
              {filteredStations.map(st => (
                <Marker 
                  key={st.id} 
                  position={[st.latitude, st.longitude]} 
                  icon={getPinIcon(st.status)}
                  eventHandlers={{
                    click: () => setActiveStation(st)
                  }}
                />
              ))}

              {activeStation && (
                <Popup 
                  position={[activeStation.latitude, activeStation.longitude]}
                  onClose={() => setActiveStation(null)}
                >
                  <div className="popup-inner">
                    <strong>{activeStation.stationName}</strong>
                    <div className="popup-buttons">
                      <button onClick={() => handleReservation(activeStation.id)} className="btn-mini-primary">Rezervasyon</button>
                      <button onClick={() => startInternalRouting(activeStation)} className="btn-mini-outline">Rota</button>
                    </div>
                  </div>
                </Popup>
              )}
            </MapContainer>
          </div>
        )}
      </section>
    </div>
  );
};

export default Dashboard;
