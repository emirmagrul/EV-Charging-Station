import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet-routing-machine';
import chargerService from '../services/chargerService';
import mapService from '../services/mapService';
import './Home.css';

// --- İkonlar ---
const userIcon = L.divIcon({
  html: `<div class="user-circle-marker"></div>`,
  className: '',
  iconSize: [16, 16],
  iconAnchor: [8, 8]
});

const getPinIcon = (status) => {
  let color = '#10b981';
  if (status === 'OCCUPIED') color = '#f59e0b';
  if (status === 'OFFLINE') color = '#ef4444';

  return L.divIcon({
    html: `<svg width="30" height="42" viewBox="0 0 30 42" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 0C6.71573 0 0 6.71573 0 15C0 26.25 15 42 15 42C15 42 30 26.25 30 15C30 6.71573 23.2843 0 15 0ZM15 20.25C12.1005 20.25 9.75 17.8995 9.75 15C9.75 12.1005 12.1005 9.75 15 9.75C17.8995 9.75 20.25 12.1005 20.25 15C20.25 17.8995 17.8995 20.25 15 20.25Z" fill="${color}"/>
            <circle cx="15" cy="15" r="5" fill="white"/>
           </svg>`,
    className: 'pin-marker',
    iconSize: [30, 42],
    iconAnchor: [15, 42],
    popupAnchor: [0, -40]
  });
};

// Rota Çizme ve Bilgi Alma Bileşeni
const RoutingMachine = ({ userCoords, targetCoords, setRouteInfo }) => {
  const map = useMap();
  const routingControlRef = useRef(null);

  useEffect(() => {
    if (!map || !userCoords || !targetCoords) return;
    if (routingControlRef.current) map.removeControl(routingControlRef.current);

    routingControlRef.current = L.Routing.control({
      waypoints: [
        L.latLng(userCoords.latitude, userCoords.longitude),
        L.latLng(targetCoords.latitude, targetCoords.longitude)
      ],
      lineOptions: { styles: [{ color: '#10b981', weight: 6, opacity: 0.8 }] },
      createMarker: () => null, // EKSTRA MAVİ İĞNELERİ BURADA KAPATIYORUZ
      addWaypoints: false,
      draggableWaypoints: false,
      fitSelectedRoutes: true,
      show: false
    }).on('routesfound', function(e) {
      const routes = e.routes;
      const summary = routes[0].summary;
      setRouteInfo({
        distance: (summary.totalDistance / 1000).toFixed(1), // km cinsinden
        time: Math.round(summary.totalTime / 60) // dakika cinsinden
      });
    }).addTo(map);

    return () => { if (routingControlRef.current) map.removeControl(routingControlRef.current); };
  }, [map, userCoords, targetCoords]);

  return null;
};

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
      <section className="hero-section">
        <h1 className="hero-title">Yolculuğun <span className="text-gradient">Hiç Bitmesin</span></h1>
        <p className="hero-subtitle">Sana en yakın istasyonu bul ve uygulama içinden hemen yol tarifi al.</p>
      </section>

      {recommended.length > 0 && (
        <section className="recommended-section">
          <div className="section-title-wrapper">
            <h2 className="section-title">Sana Özel Önerilenler</h2>
            <div className="title-line"></div>
          </div>
          <div className="recommended-scroll">
            {recommended.map(st => (
              <div key={st.id} className="glass-panel recommended-card">
                <div className="rec-badge">Yakınında</div>
                <h3>{st.stationName}</h3>
                <div className="card-actions-row vertical">
                  <button className="btn-primary-new" onClick={() => handleReservation(st.id)}>Rezervasyon Yap</button>
                  <div className="card-secondary-actions">
                    <button className="btn-outline-mini" onClick={() => navigate(`/stations/${st.id}`)}>Detay</button>
                    <button className="btn-outline-mini" onClick={() => startInternalRouting(st)}>Rota</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

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
