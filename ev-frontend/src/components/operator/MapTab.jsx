import React from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { getPinIcon } from '../../utils/operatorHelpers';

function MapTab({ stations, chargers }) {
  const safeStations = Array.isArray(stations) ? stations : [];
  
  const getStationStatus = (stationId) => {
    const stationChargers = chargers.filter(c => c.stationId === stationId);
    if (stationChargers.length === 0) return 'OFFLINE';
    if (stationChargers.some(c => c.status === 'AVAILABLE')) return 'AVAILABLE';
    if (stationChargers.some(c => c.status === 'CHARGING')) return 'CHARGING';
    return 'OUT_OF_SERVICE';
  };

  const center = safeStations.length > 0 
    ? [safeStations[0].latitude, safeStations[0].longitude] 
    : [38.4237, 27.1428];

  return (
    <div className="operator-map-container glass-panel" style={{ marginTop: '20px', overflow: 'hidden' }}>
      <MapContainer center={center} zoom={12} style={{ height: '500px', width: '100%', borderRadius: '12px' }}>
        <TileLayer url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png" />
        {safeStations.map(st => (
          <Marker 
            key={st.id} 
            position={[st.latitude, st.longitude]} 
            icon={getPinIcon(getStationStatus(st.id))}
          >
            <Popup>
              <div className="op-map-popup">
                <strong>{st.stationName}</strong>
                <p>{st.address}</p>
                <div className="popup-stats" style={{ marginTop: '5px', fontSize: '0.9em', color: 'var(--text-muted)' }}>
                  <span>Ünite Sayısı: {chargers.filter(c => c.stationId === st.id).length}</span>
                </div>
              </div>
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}

export default MapTab;
