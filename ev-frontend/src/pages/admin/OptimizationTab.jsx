import React from 'react';

const OptimizationTab = ({ 
  stations, 
  peakHours, 
  revenueData, 
  healthData, 
  selectedStationForPeak, 
  onStationChange 
}) => {
  return (
    <div className="dashboard-section glass-panel">
      <h2 className="section-title">2. Ağ Performansı Optimizasyonu</h2>
      
      <div style={{marginBottom: '40px'}}>
        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '32px'}}>
          <div>
            <h3 style={{fontSize: '1.4rem', marginBottom: '8px'}}>Yoğun Saat Analizi (Peak Hour Analysis)</h3>
            <p style={{color: 'var(--text-muted)', fontSize: '0.95rem'}}>
              Ağ trafiğinin zirve yaptığı zaman dilimlerini "Isı Haritası" üzerinden takip edin.
            </p>
          </div>
          <div className="premium-select-wrapper">
            <select 
              className="premium-select" 
              value={selectedStationForPeak}
              onChange={(e) => onStationChange(e.target.value)}
            >
              <option value="global">Tüm Ağ (Global)</option>
              {stations.map(s => (
                <option key={s.id} value={s.id}>{s.stationName}</option>
              ))}
            </select>
          </div>
        </div>
        
        <div className="peak-hours-grid">
          {Array.from({length: 24}).map((_, i) => {
            const count = peakHours ? peakHours[i] || 0 : 0;
            const maxCount = peakHours ? Math.max(...Object.values(peakHours), 1) : 1;
            const intensity = (count / maxCount);
            
            return (
              <div 
                key={i} 
                className={`hour-slot ${count > 0 ? 'active' : ''}`}
                style={{
                  background: count > 0 ? `rgba(16, 185, 129, ${0.1 + intensity * 0.4})` : '',
                  borderColor: intensity > 0.8 ? 'var(--primary)' : ''
                }}
              >
                <strong>{i}:00</strong>
                <span>{count}</span>
              </div>
            );
          })}
        </div>
      </div>

      <div>
        <h3>Stratejik Karar Destek</h3>
        <div className="stats-grid" style={{marginTop: '20px'}}>
          <div className="stat-card" style={{background: 'rgba(255,255,255,0.02)'}}>
            <span className="stat-label">Ortalama Ağ Doluluğu</span>
            <span className="stat-value">
              %{ (Object.values(revenueData?.occupancyRateByStation || {}).reduce((a, b) => a + b, 0) / 
                  Object.values(revenueData?.occupancyRateByStation || {}).length || 0).toFixed(1) }
            </span>
          </div>
          <div className="stat-card" style={{background: 'rgba(255,255,255,0.02)'}}>
            <span className="stat-label">Toplam İstasyon Sayısı</span>
            <span className="stat-value">{healthData?.totalStations}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OptimizationTab;
