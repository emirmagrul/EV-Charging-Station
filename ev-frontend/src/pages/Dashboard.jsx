import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import { useGeolocation } from '../hooks/useGeolocation';
import { useStations } from '../hooks/useStations';
import StationCard from '../components/dashboard/StationCard';
import UserStats from '../components/dashboard/UserStats';
import ReservationsModal from '../components/dashboard/ReservationsModal';
import ReservationModal from '../components/dashboard/ReservationsModal';
import StationShelf from '../components/dashboard/StationShelf';
import DashboardMap from '../components/dashboard/DashboardMap';
import ActiveChargingSession from '../components/dashboard/ActiveChargingSession';
import reservationService from '../services/reservationService';
import sessionService from '../services/sessionService';
import mapService from '../services/mapService';
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
  const [activeStationInfo, setActiveStationInfo] = useState(null); // renamed from activeStation to avoid conflict with activeSession
  const [allReservations, setAllReservations] = useState([]);
  const [showReservationsModal, setShowReservationsModal] = useState(false);
  const [resTab, setResTab] = useState('active'); // 'active' or 'history'
  const [activeSession, setActiveSession] = useState(null);
  const [recommendedStations, setRecommendedStations] = useState([]);

  useEffect(() => {
    if (user) {
      reservationService.getMyReservations(user.id).then(res => {
        setAllReservations(res);
      }).catch(err => console.error(err));

      sessionService.getActiveSession(user.id).then(res => {
        setActiveSession(res);
      }).catch(err => console.error(err));
    }
  }, [user]);

  const handleSessionEnd = () => {
    setActiveSession(null);
    refreshUser(); // update wallet balance
    if (user) {
      reservationService.getMyReservations(user.id).then(res => {
        setAllReservations(res);
      }).catch(err => console.error(err));
    }
  };

  const handleStartSession = async (reservationId) => {
    try {
      const newSession = await sessionService.startSession(reservationId);
      setActiveSession(newSession);
      setShowReservationsModal(false); // Modal'ı kapat
      // Rezervasyon listesini yenile
      reservationService.getMyReservations(user?.id).then(res => setAllReservations(res));
      alert("Şarj başarıyla başlatıldı!");
    } catch (error) {
      alert(error.response?.data || error.message || "Şarj başlatılamadı.");
    }
  };

  useEffect(() => {
    if (userCoords && stations.length > 0) {
      setMapCenter([userCoords.latitude, userCoords.longitude]);
      // Fetch nearby stations for recommended section
      mapService.getNearbyStations(userCoords.latitude, userCoords.longitude)
        .then(data => {
          // Backend'den gelen veride adres eksik olabiliyor. 
          // Bu yüzden ID'leri eşleştirip ana 'stations' listesinden tam veriyi (adres dahil) alıyoruz.
          const enrichedStations = data.slice(0, 4).map(nearby => {
            const fullStation = stations.find(s => s.id === nearby.id);
            return fullStation || nearby;
          });
          setRecommendedStations(enrichedStations);
        })
        .catch(err => console.error("Yakın istasyonlar yüklenemedi:", err));
    } else if (userCoords) {
       setMapCenter([userCoords.latitude, userCoords.longitude]);
    }
  }, [userCoords, stations]);

  // Arka planda istasyon verisi değişirse (örn. polling) aktif popup'ı güncelle
  useEffect(() => {
    if (activeStationInfo) {
      const fresh = stations.find(s => s.id === activeStationInfo.id);
      if (fresh && fresh.status !== activeStationInfo.status) {
        setActiveStationInfo(fresh);
      }
    }
  }, [stations, activeStationInfo]);

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




  const startInternalRouting = (st) => {
    if (!userCoords) return alert("Konum izni gerekiyor.");
    setTargetRoute({ latitude: st.latitude, longitude: st.longitude });
    setViewMode('map');
    setMapCenter([st.latitude, st.longitude]);
    setActiveStationInfo(null);
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
    setActiveStationInfo(freshStation);
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

      {/* Aktif Şarj Oturumu */}
      {activeSession && (
        <ActiveChargingSession 
          session={activeSession} 
          onSessionEnd={handleSessionEnd} 
        />
      )}

      {/* Önerilen İstasyonlar */}
      <StationShelf 
        title="Önerilen İstasyonlar"
        stations={recommendedStations}
        onStationClick={goToStation}
        badge="Önerilen"
      />

      {/* Favori İstasyonlar Bölümü */}
      {user?.role === 'DRIVER' && (
        <StationShelf 
          title="Favori İstasyonlarım"
          stations={favorites}
          onStationClick={goToStation}
          icon="❤️"
          actionButton={<button className="btn-text" onClick={() => setShowFavoritesModal(true)}>Düzenle</button>}
        />
      )}

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
                showFavoriteBtn={user?.role === 'DRIVER'}
              />
            ))}
          </div>
        ) : (
          <DashboardMap 
            userCoords={userCoords}
            stations={filteredStations}
            activeStation={activeStationInfo}
            setActiveStation={setActiveStationInfo}
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
        onStartSession={handleStartSession}
        activeSession={activeSession}
      />


    </div>
  );
};

export default Dashboard;
