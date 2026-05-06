import React from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { RoutingMachine, ChangeView } from '../map/MapLayers';
import { userIcon, getPinIcon } from '../map/MapIcons';

const DashboardMap = ({ 
  userCoords, 
  stations, 
  activeStation, 
  setActiveStation, 
  targetRoute, 
  setTargetRoute, 
  routeInfo, 
  setRouteInfo, 
  mapCenter,
  onReserve,
  onRoute 
}) => {
  return (
    <div className="map-wrapper glass-panel" style={{ position: 'relative' }}>
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
          <button className="close-route" onClick={() => { setTargetRoute(null); setRouteInfo(null); }}>✕</button>
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

        {stations.map(st => (
          <Marker
            key={`${st.id}-${st.status}`} // Status değiştiğinde marker'ı yeniden çizmeye zorla
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
              <div style={{ fontSize: '0.75rem', marginBottom: '8px' }}>
                {activeStation.status === 'AVAILABLE' && <span style={{ color: 'var(--primary)' }}>✅ Müsait — En az bir cihaz boşta</span>}
                {activeStation.status === 'OCCUPIED' && <span style={{ color: 'orange' }}>🟠 Dolu — Tüm cihazlar şu an şarjda</span>}
                {activeStation.status === 'OFFLINE' && <span style={{ color: '#ef4444' }}>🔴 Bakımda — Hizmet dışı</span>}
              </div>
              <div className="popup-buttons">
                {activeStation.status === 'AVAILABLE' && (
                  <button onClick={() => onReserve(activeStation.id)} className="btn-mini-primary">Rezervasyon</button>
                )}
                {activeStation.status === 'AVAILABLE' && (
                  <button onClick={() => onRoute(activeStation)} className="btn-mini-outline">Rota</button>
                )}
                {activeStation.status !== 'AVAILABLE' && (
                  <span style={{ fontSize: '0.8rem', color: '#888' }}>Şu an kullanılamıyor</span>
                )}
              </div>
            </div>
          </Popup>
        )}
      </MapContainer>
    </div>
  );
};

export default DashboardMap;
