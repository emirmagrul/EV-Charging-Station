import React, { useState } from 'react';
import { STATUS_LABELS } from '../../utils/operatorHelpers';

function StatusTab({ stations, chargers, loadingChargers, onStatusChange }) {
  const [selectedStation, setSelectedStation] = useState('');

  const safeStations = Array.isArray(stations) ? stations : [];
  const safeChargers = Array.isArray(chargers) ? chargers : [];

  const filteredChargers = selectedStation
    ? safeChargers.filter(c => String(c.stationId) === selectedStation)
    : safeChargers;

  return (
    <div>
      <div className="station-selector">
        <label>İstasyon Filtresi:</label>
        <select value={selectedStation} onChange={e => setSelectedStation(e.target.value)}>
          <option value="">Tüm İstasyonlar</option>
          {safeStations.map(s => (
            <option key={s.id} value={String(s.id)}>{s.stationName}</option>
          ))}
        </select>
      </div>

      {loadingChargers ? (
        <div className="op-loading"><div className="spin" /><span>Üniteler yükleniyor…</span></div>
      ) : filteredChargers.length === 0 ? (
        <div className="op-empty-state">
          <div className="empty-icon">🔌</div>
          <p>Bu istasyona ait şarj ünitesi bulunamadı.</p>
        </div>
      ) : (
        <div className="charger-grid">
          {filteredChargers.map(charger => {
            const statusInfo = STATUS_LABELS[charger.status] || { label: charger.status, color: 'gray' };
            const isOos = charger.status === 'OUT_OF_SERVICE' || charger.status === 'OFFLINE';

            return (
              <div key={charger.id} className={`charger-card ${charger.status}`}>
                <div className="charger-card-header">
                  <div className="charger-id-info">
                    <h3>Ünite #{charger.id}</h3>
                    <span>{charger.chargerType || 'AC Tip-2'} · {charger.powerOutput || '22'} kW</span>
                  </div>
                  <div className={`charger-status-badge badge-${charger.status}`}>
                    {statusInfo.label}
                  </div>
                </div>

                <div className="charger-details">
                  <div className="charger-detail-row">
                    <span>İstasyon</span>
                    <span>{charger.stationName || `#${charger.stationId}`}</span>
                  </div>
                  <div className="charger-detail-row">
                    <span>Konnektör</span>
                    <span>{charger.connectorType?.name || 'Type 2'}</span>
                  </div>
                  <div className="charger-detail-row">
                    <span>Fiyat</span>
                    <span>{charger.pricePerKwh || '—'} ₺/kWh</span>
                  </div>
                </div>

                <div className="charger-actions">
                  {!isOos ? (
                    <button
                      className="action-btn disable"
                      onClick={() => onStatusChange(charger, 'OUT_OF_SERVICE')}
                    >
                      🔴 Hizmet Dışı Bırak
                    </button>
                  ) : (
                    <button
                      className="action-btn enable"
                      onClick={() => onStatusChange(charger, 'AVAILABLE')}
                    >
                      🟢 Hizmete Al
                    </button>
                  )}
                  <button
                    className="action-btn offline"
                    onClick={() => onStatusChange(charger, 'OFFLINE')}
                    disabled={charger.status === 'OFFLINE'}
                  >
                    🔌 Çevrimdışı
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default StatusTab;
