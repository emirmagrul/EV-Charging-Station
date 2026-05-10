import React from 'react';

const OverviewTab = ({ revenueData, activityData, healthData, reservations }) => {
  return (
    <>
      <div className="stats-grid">
        <div className="stat-card glass-panel">
          <span className="stat-label">Toplam Ağ Geliri</span>
          <span className="stat-value">{(revenueData?.totalNetworkRevenue || 0).toLocaleString('tr-TR')} ₺</span>
          <div className="stat-trend">Genel ağ geliri</div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-label">Aktif Sürücüler</span>
          <span className="stat-value">{activityData?.totalDrivers || 0}</span>
          <div className="stat-trend">Kayıtlı toplam kullanıcı</div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-label">Aktif Rezervasyonlar</span>
          <span className="stat-value">{activityData?.activeReservations || 0}</span>
          <div className="stat-trend">Sistemdeki bekleyen/onaylı</div>
        </div>
        <div className="stat-card glass-panel">
          <span className="stat-label">Ağ Erişilebilirliği</span>
          <span className="stat-value" style={{color: 'var(--success)'}}>
            {healthData?.totalChargers > 0 ? (100 - (healthData.outOfServiceChargers / healthData.totalChargers * 100)).toFixed(1) : '100'}%
          </span>
          <div className="stat-trend">Operasyonel durum</div>
        </div>
      </div>

      <div className="dashboard-main-grid">
        <div className="dashboard-section glass-panel">
          <h2 className="section-title">Ağ Performans Özeti</h2>
          <div className="revenue-list">
            {revenueData?.revenueByStation && Object.entries(revenueData.revenueByStation).length > 0 ? (
              Object.entries(revenueData.revenueByStation).map(([station, revenue]) => {
                const maxRevenue = Math.max(...Object.values(revenueData.revenueByStation), 1);
                return (
                  <div className="revenue-item" key={station}>
                    <div className="revenue-item-info">
                      <span>{station}</span>
                      <strong>{revenue.toLocaleString('tr-TR')} ₺</strong>
                    </div>
                    <div className="progress-bar-bg">
                      <div 
                        className="progress-bar-fill" 
                        style={{width: `${(revenue / maxRevenue) * 100}%`}}
                      ></div>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="upcoming-reservations">
                {reservations.filter(r => r.status === 'CONFIRMED' || r.status === 'PENDING').length > 0 ? (
                  <div className="res-list">
                    <p className="res-subtitle">Henüz seans verisi yok, ancak aktif rezervasyonlar mevcut:</p>
                    {reservations
                      .filter(r => r.status === 'CONFIRMED' || r.status === 'PENDING')
                      .slice(0, 5)
                      .map(res => (
                        <div className="res-item glass-panel-mini" key={res.id}>
                          <div className="res-info">
                            <strong>{res.stationName}</strong>
                            <span>{res.driverName}</span>
                          </div>
                          <div className="res-time">
                            {res.reservationDate} | {res.startTime.substring(0,5)}
                          </div>
                          <div className={`res-status-tag ${res.status.toLowerCase()}`}>
                            {res.status === 'CONFIRMED' ? 'ONAYLI' : 'BEKLEYEN'}
                          </div>
                        </div>
                      ))
                    }
                  </div>
                ) : (
                  <div className="empty-state">Henüz ağ üzerinde bir faaliyet bulunmuyor.</div>
                )}
              </div>
            )}
          </div>
        </div>
        <div className="dashboard-section glass-panel">
          <h2 className="section-title">Sistem Sağlığı</h2>
          <div className="health-grid">
            <div className="health-item">
              <span>İstasyon Sayısı</span>
              <strong>{healthData?.totalStations || 0}</strong>
            </div>
            <div className="health-item">
              <span>Şarj Üniteleri</span>
              <strong>{healthData?.totalChargers || 0}</strong>
            </div>
            <div className="health-item">
              <span>Servis Dışı</span>
              <strong style={{color: (healthData?.outOfServiceChargers || 0) > 0 ? 'var(--danger)' : 'var(--success)'}}>
                {healthData?.outOfServiceChargers || 0}
              </strong>
            </div>
            <div className="health-item">
              <span>Arıza Kayıtları</span>
              <strong style={{color: (healthData?.pendingIssueReports || 0) > 0 ? 'var(--warning)' : 'var(--text-main)'}}>
                {healthData?.pendingIssueReports || 0}
              </strong>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default OverviewTab;
