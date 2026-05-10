import React from 'react';

const ConfigTab = ({ 
  stations, 
  configChanges, 
  updateStatus, 
  handleLocalChange, 
  handleConfigUpdate 
}) => {
  return (
    <div className="dashboard-section glass-panel">
      <h2 className="section-title">3. Sistem Sağlığı ve Konfigürasyonu</h2>
      <p style={{color: 'var(--text-muted)', marginBottom: '24px', fontSize: '0.9rem'}}>
        İstasyon bazlı fiyatlandırma ve çalışma saatlerini yöneterek iş mantığını yapılandırın.
      </p>
      
      <div className="config-list">
        {stations.map(station => {
          const changes = configChanges[station.id] || { pricing: station.pricingPerKWh, hours: station.operatingHours };
          
          return (
            <div className="station-config-card glass-panel-mini" key={station.id}>
              <div className="station-info">
                <h3>{station.stationName}</h3>
                <p>{station.locationName}</p>
              </div>

              <div className="config-controls">
                <div className="control-group">
                  <label>Fiyat (₺/kWh)</label>
                  <div className="custom-number-input">
                    <button onClick={() => handleLocalChange(station.id, 'pricing', (parseFloat(changes.pricing) - 0.25).toFixed(2))}>-</button>
                    <input 
                      type="number" 
                      step="0.01"
                      value={changes.pricing} 
                      onChange={(e) => handleLocalChange(station.id, 'pricing', e.target.value)}
                    />
                    <button onClick={() => handleLocalChange(station.id, 'pricing', (parseFloat(changes.pricing) + 0.25).toFixed(2))}>+</button>
                  </div>
                </div>

                <div className="control-group">
                  <label>Çalışma Saatleri</label>
                  <input 
                    type="text" 
                    value={changes.hours} 
                    placeholder="Örn: 08:00-22:00"
                    onChange={(e) => handleLocalChange(station.id, 'hours', e.target.value)}
                  />
                </div>

                <button 
                  className="primary-btn" 
                  style={{padding: '12px 24px'}}
                  onClick={() => handleConfigUpdate(station.id)}
                  disabled={updateStatus.id === station.id}
                >
                  {updateStatus.id === station.id ? '...' : 'Güncelle'}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default ConfigTab;
