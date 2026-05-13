import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet-routing-machine';
import chargerService from '../services/chargerService';
import mapService from '../services/mapService';
import './Home.css';

import { RoutingMachine } from '../components/map/MapLayers';
import { userIcon, getPinIcon } from '../components/map/MapIcons';
import HeroSection from '../components/home/HeroSection';
import RecommendedSection from '../components/home/RecommendedSection';

const Home = () => {
  const navigate = useNavigate();
  const [stations, setStations] = useState([]);
  const [recommended, setRecommended] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [userCoords, setUserCoords] = useState(null);
  const [viewMode, setViewMode] = useState('list');
  const [targetRoute, setTargetRoute] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);

  useEffect(() => {
    chargerService.getAllStations()
      .then(data => { setStations(data); setLoading(false); })
      .catch(() => setLoading(false));

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((position) => {
        const { latitude, longitude } = position.coords;
        setUserCoords({ latitude, longitude });
        mapService.getNearbyStations(latitude, longitude)
          .then(data => setRecommended(data.slice(0, 4)));
      });
    }
  }, []);

  const filteredStations = stations.filter(st => 
    st.stationName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    st.address?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const startInternalRouting = (st) => {
    if (!userCoords) return alert("Konum izni gerekiyor.");
    setTargetRoute({ latitude: st.latitude, longitude: st.longitude });
    setViewMode('map');
  };

  const handleReservation = (stationId) => {
    const token = localStorage.getItem('token'); // Basit auth kontrolü
    if (!token) {
      alert("Rezervasyon yapmak için önce giriş yapmalısınız.");
      navigate('/login');
    } else {
      navigate(`/reserve/${stationId}`);
    }
  };

  return (
    <div className="home-container">
      <HeroSection />

      <RecommendedSection 
        recommended={recommended} 
        handleReservation={handleReservation} 
        startInternalRouting={startInternalRouting} 
        navigate={navigate} 
      />

      <section className="stations-section">
        <div className="section-header-flex">
          <h2>İstasyon Kataloğu</h2>
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

        {loading ? (
          <div className="loading-state">Yükleniyor...</div>
        ) : (
          viewMode === 'list' ? (
            <div className="station-grid">
              {filteredStations.map(station => (
                <div key={station.id} className="glass-panel premium-card">
                  <div className="card-top-icon">⚡</div>
                  <h3>{station.stationName}</h3>
                  <p className="address-text">📍 {station.address}</p>
                  <div className="premium-card-footer">
                    <button className="btn-primary-new" onClick={() => handleReservation(station.id)}>
                      ⚡ Rezervasyon Yap
                    </button>
                    <div className="card-secondary-actions">
                      <button className="btn-outline-mini" onClick={() => navigate(`/stations/${station.id}`)}>Detay</button>
                      <button className="btn-outline-mini" onClick={() => startInternalRouting(station)}>Yol Tarifi</button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="map-wrapper glass-panel" style={{position: 'relative'}}>
              {/* Mesafe ve Süre Paneli */}
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
                center={userCoords ? [userCoords.latitude, userCoords.longitude] : [38.4237, 27.1428]} 
                zoom={12} 
                style={{ height: '550px', width: '100%' }}
              >
                <TileLayer url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" />
                
                {targetRoute && userCoords && (
                  <RoutingMachine userCoords={userCoords} targetCoords={targetRoute} setRouteInfo={setRouteInfo} />
                )}

                {userCoords && (
                  <Marker position={[userCoords.latitude, userCoords.longitude]} icon={userIcon}>
                    <Popup><strong>Buradasınız</strong></Popup>
                  </Marker>
                )}

                {filteredStations.map(st => (
                  <Marker key={st.id} position={[st.latitude, st.longitude]} icon={getPinIcon(st.status)}>
                    <Popup>
                      <div className="popup-inner">
                        <strong>{st.stationName}</strong>
                        <div className="popup-buttons">
                          <button onClick={() => navigate(`/stations/${st.id}`)} className="btn-mini-primary">Detay</button>
                          <button onClick={() => handleReservation(st.id)} className="btn-mini-outline">Rezervasyon</button>
                          <button onClick={() => startInternalRouting(st)} className="btn-mini-outline">Rota</button>
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                ))}
              </MapContainer>
            </div>
          )
        )}
      </section>
    </div>
  );
};

export default Home;
