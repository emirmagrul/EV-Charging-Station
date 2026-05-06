import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import { useGeolocation } from '../hooks/useGeolocation';
import { useStations } from '../hooks/useStations';
import StationCard from '../components/dashboard/StationCard';
import UserStats from '../components/dashboard/UserStats';
import ReservationsModal from '../components/dashboard/ReservationsModal';
import StationShelf from '../components/dashboard/StationShelf';
import DashboardMap from '../components/dashboard/DashboardMap';
import reservationService from '../services/reservationService';
import './Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const { user, favorites, toggleFavorite, addBalance, refreshUser } = useAuth();

  const { 
    selectedStation, 
    setShowFavoritesModal,
    selectedVehicle, setSelectedVehicle
  } = useUI();

  
  const { stations, loading: stationsLoading } = useStations();
  const { coords: userCoords } = useGeolocation();

  const [searchTerm, setSearchTerm] = useState('');
  const [viewMode, setViewMode] = useState('map');
  const [targetRoute, setTargetRoute] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
  const [mapCenter, setMapCenter] = useState(null);
  const [activeStation, setActiveStation] = useState(null);
  const [allReservations, setAllReservations] = useState([]);
  const [showReservationsModal, setShowReservationsModal] = useState(false);
  const [resTab, setResTab] = useState('active'); // 'active' or 'history'

  useEffect(() => {
    if (user) {
      reservationService.getMyReservations(user.id).then(res => {
        setAllReservations(res);
      }).catch(err => console.error(err));
    }
  }, [user]);

  useEffect(() => {
    if (userCoords) {
      setMapCenter([userCoords.latitude, userCoords.longitude]);
    }
  }, [userCoords]);

  // Arka planda istasyon verisi değişirse (örn. polling) aktif popup'ı güncelle
  useEffect(() => {
    if (activeStation) {
      const fresh = stations.find(s => s.id === activeStation.id);
      if (fresh && fresh.status !== activeStation.status) {
        setActiveStation(fresh);
      }
    }
  }, [stations, activeStation]);

  useEffect(() => {
    if (selectedStation) {
      goToStation(selectedStation);
    }
  }, [selectedStation]);

  const filteredStations = stations.filter(st => {
    const matchesSearch = st.stationName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          st.address?.toLowerCase().includes(searchTerm.toLowerCase());
    
    // Araç uyumluluk filtresi
    const matchesVehicle = !selectedVehicle || 
                          (st.supportedConnectorTypeIds && st.supportedConnectorTypeIds.includes(Number(selectedVehicle.connectorTypeId)));
    
    return matchesSearch && matchesVehicle;
  });


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
    // Listeden en güncel veriyi bul (statü senkronizasyonu için kritik)
    const freshStation = stations.find(s => s.id === st.id) || st;
    
    setTargetRoute(null);
    setRouteInfo(null);
    setViewMode('map');
    setMapCenter([freshStation.latitude, freshStation.longitude]);
    setActiveStation(freshStation);
  };

  return (
    <div className="dashboard-container">
      {/* Üst Panel */}
      <header className="dashboard-header">
        <div className="welcome-text">
          <h1>Merhaba, <span className="text-gradient">{user?.firstName || 'Sürücü'}</span></h1>
          <p>Yolculuğun için enerji dolu bir gün dileriz.</p>
        </div>

        <UserStats 
          user={user} 
          addBalance={addBalance} 
          activeCount={allReservations.filter(r => r.status === 'PENDING' || r.status === 'CONFIRMED').length}
          onShowReservations={() => { setResTab('active'); setShowReservationsModal(true); }}
        />
      </header>

      {/* Önerilen İstasyonlar */}
      <StationShelf 
        title="Önerilen İstasyonlar"
        stations={recommendedStations}
        onStationClick={goToStation}
        badge="Önerilen"
      />

      {/* Favori İstasyonlar Bölümü */}
      <StationShelf 
        title="Favori İstasyonlarım"
        stations={favorites}
        onStationClick={goToStation}
        icon="❤️"
        actionButton={<button className="btn-text" onClick={() => setShowFavoritesModal(true)}>Düzenle</button>}
      />

      {/* Ana İstasyon Alanı */}
      <section className="stations-main-section">
        <div className="section-header-flex">
          <div className="title-group">
            <h2>İstasyon Bul</h2>
            <p>Konumuna en yakın ve aracına uygun istasyonlar.</p>
            {selectedVehicle && (
              <div className="filter-badge-active">
                <span>{selectedVehicle.brand} {selectedVehicle.model} için filtrelendi</span>
                <button onClick={() => setSelectedVehicle(null)}>✕</button>
              </div>
            )}
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
          <DashboardMap 
            userCoords={userCoords}
            stations={filteredStations}
            activeStation={activeStation}
            setActiveStation={setActiveStation}
            targetRoute={targetRoute}
            setTargetRoute={setTargetRoute}
            routeInfo={routeInfo}
            setRouteInfo={setRouteInfo}
            mapCenter={mapCenter}
            onReserve={handleReservation}
            onRoute={startInternalRouting}
          />
        )}
      </section>

      <ReservationsModal 
        isOpen={showReservationsModal}
        onClose={() => setShowReservationsModal(false)}
        reservations={allReservations}
        setAllReservations={setAllReservations}
        refreshUser={refreshUser}
      />


    </div>
  );
};

export default Dashboard;
