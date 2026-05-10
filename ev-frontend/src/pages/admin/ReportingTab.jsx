import React from 'react';

const ReportingTab = ({ stations, revenueData }) => {
  const sortedByRevenue = [...stations].sort((a, b) => 
    (revenueData?.revenueByStation[b.stationName] || 0) - (revenueData?.revenueByStation[a.stationName] || 0)
  );
  const sortedBySessions = [...stations].sort((a, b) => 
    (revenueData?.historicalUsageCountByStation?.[b.stationName] || 0) - (revenueData?.historicalUsageCountByStation?.[a.stationName] || 0)
  );

  return (
    <div className="dashboard-section glass-panel">
      <h2 className="section-title">1. İdari Raporlama ve Analiz</h2>
      
      <div className="stats-grid" style={{marginBottom: '32px', gridTemplateColumns: 'repeat(2, 1fr)'}}>
        <div className="stat-card" style={{background: 'rgba(16, 185, 129, 0.05)', border: '1px solid var(--primary-glow)'}}>
          <span className="stat-label">⭐ En Çok Gelir Getiren İstasyon</span>
          <span className="stat-value" style={{fontSize: '1.5rem'}}>{sortedByRevenue[0]?.stationName || '-'}</span>
          <span className="stat-trend">{revenueData?.revenueByStation?.[sortedByRevenue[0]?.stationName]?.toLocaleString('tr-TR') || 0} ₺</span>
        </div>
        <div className="stat-card" style={{background: 'rgba(59, 130, 246, 0.05)', border: '1px solid rgba(59, 130, 246, 0.2)'}}>
          <span className="stat-label">🔥 En Çok Kullanılan İstasyon</span>
          <span className="stat-value" style={{fontSize: '1.5rem'}}>{sortedBySessions[0]?.stationName || '-'}</span>
          <span className="stat-trend">{revenueData?.historicalUsageCountByStation?.[sortedBySessions[0]?.stationName] || 0} Seans</span>
        </div>
      </div>

      <p style={{color: 'var(--text-muted)', marginBottom: '24px'}}>
        Sistemdeki tüm istasyonların ({stations.length} adet) performans dökümü.
      </p>
      
      <div className="admin-table-container">
        <table className="admin-table">
          <thead>
            <tr>
              <th>İstasyon Adı</th>
              <th>Toplam Gelir</th>
              <th>Beklenen Gelir</th>
              <th>Aktif Şarj (Seans)</th>
              <th>Gelecek Rez</th>
              <th>Şebeke Yük Tahmini</th>
            </tr>
          </thead>
          <tbody>
            {stations.map(station => (
              <tr key={station.id}>
                <td><strong>{station.stationName}</strong></td>
                <td>{revenueData?.revenueByStation?.[station.stationName]?.toLocaleString('tr-TR') || 0} ₺</td>
                <td style={{color: 'var(--primary)'}}>
                  {revenueData?.pendingRevenueByStation?.[station.stationName]?.toLocaleString('tr-TR') || 0} ₺
                </td>
                <td>{revenueData?.sessionCountByStation?.[station.stationName] || 0} Seans</td>
                <td>{revenueData?.reservationCountByStation?.[station.stationName] || 0} Adet</td>
                <td>
                  <span style={{color: (revenueData?.occupancyRateByStation?.[station.stationName] > 70) ? 'var(--danger)' : 'var(--success)'}}>
                    %{revenueData?.occupancyRateByStation?.[station.stationName]?.toFixed(1) || '0.0'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ReportingTab;
