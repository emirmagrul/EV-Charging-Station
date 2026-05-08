import React, { useState } from 'react';
import { FAULT_STATUS_LABELS, formatDate } from '../../utils/operatorHelpers';

function FaultTab({ faultReports, loadingFaults, chargers, onUpdateFaultStatus, onDeactivateCharger }) {
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [reporterFilter, setReporterFilter] = useState('ALL'); // ALL, DRIVER, OPERATOR

  const safeFaultReports = Array.isArray(faultReports) ? faultReports : [];
  
  const filtered = safeFaultReports.filter(f => {
    // Statü Filtresi
    let matchStatus = false;
    if (filterStatus === 'ALL') matchStatus = true;
    else if (filterStatus === 'OPEN') matchStatus = (f.status === 'OPEN' || f.status === 'PENDING');
    else if (filterStatus === 'HISTORY') matchStatus = (f.status === 'RESOLVED' || f.status === 'DISMISSED');
    else matchStatus = f.status === filterStatus;
    
    // Kaynak Filtresi
    let matchReporter = true;
    if (reporterFilter === 'DRIVER') matchReporter = !!f.driverId;
    if (reporterFilter === 'OPERATOR') matchReporter = !!f.operatorId;
    
    return matchStatus && matchReporter;
  });

  const priorityOf = (report) => {
    if (report.priority) return report.priority;
    if (report.description?.toLowerCase().includes('ödeme') || report.description?.toLowerCase().includes('güvenlik')) return 'HIGH';
    if (report.description?.toLowerCase().includes('soket')) return 'MEDIUM';
    return 'LOW';
  };

  return (
    <div>
      <div className="fault-filter-row">
        <div className="fault-filter-bar">
          <button
            className={`filter-chip ${filterStatus === 'ALL' ? 'active' : ''}`}
            onClick={() => setFilterStatus('ALL')}
          >
            Tümü
          </button>
          <button
            className={`filter-chip ${filterStatus === 'OPEN' ? 'active' : ''}`}
            onClick={() => setFilterStatus('OPEN')}
          >
            Açık / Beklemede
          </button>
          <button
            className={`filter-chip ${filterStatus === 'IN_PROGRESS' ? 'active' : ''}`}
            onClick={() => setFilterStatus('IN_PROGRESS')}
          >
            İşlemde
          </button>
          <button
            className={`filter-chip ${filterStatus === 'HISTORY' ? 'active' : ''}`}
            onClick={() => setFilterStatus('HISTORY')}
          >
            📜 Geçmiş Raporlar
          </button>
        </div>

        <div className="fault-filter-bar source-filter">
          <button 
            className={`filter-chip mini ${reporterFilter === 'ALL' ? 'active' : ''}`}
            onClick={() => setReporterFilter('ALL')}
          >
            Tüm Kaynaklar
          </button>
          <button 
            className={`filter-chip mini ${reporterFilter === 'DRIVER' ? 'active' : ''}`}
            onClick={() => setReporterFilter('DRIVER')}
          >
            Sürücülerden
          </button>
          <button 
            className={`filter-chip mini ${reporterFilter === 'OPERATOR' ? 'active' : ''}`}
            onClick={() => setReporterFilter('OPERATOR')}
          >
            Operatörden
          </button>
        </div>
      </div>

      {loadingFaults ? (
        <div className="op-loading"><div className="spin" /><span>Raporlar yükleniyor…</span></div>
      ) : filtered.length === 0 ? (
        <div className="op-empty-state">
          <div className="empty-icon">✅</div>
          <p>Bu filtrede arıza raporu bulunamadı.</p>
        </div>
      ) : (
        <div className="fault-list">
          {filtered.map(report => {
            const priority = priorityOf(report);
            return (
              <div key={report.id} className="fault-card glass-panel">
                <div className={`fault-priority-dot priority-${priority}`} title={`Öncelik: ${priority}`} />

                <div className="fault-info">
                  <h3>
                    Ünite #{report.chargerId} {report.stationName ? `— ${report.stationName}` : ''}
                  </h3>
                  <p>{report.description || 'Açıklama girilmemiş.'}</p>
                  <div className="fault-meta">
                    <span className={`reporter-badge ${report.operatorId ? 'op' : 'dr'}`}>
                      {report.operatorId ? '🛡️ Operatör' : '👤 Sürücü'}
                    </span>
                    <span>{report.reporterName || `Kullanıcı #${report.driverId || report.operatorId}`}</span>
                    <span>🕐 {formatDate(report.reportedAt)}</span>
                    <span>
                      📌 Durum:&nbsp;
                      <strong style={{ color: 'var(--text-main)' }}>
                        {FAULT_STATUS_LABELS[report.status] || report.status}
                      </strong>
                    </span>
                  </div>
                </div>

                <div className="fault-actions-new">
                  {report.status === 'RESOLVED' || report.status === 'DISMISSED' ? (
                    <div className={`result-badge ${report.status.toLowerCase()}`}>
                      {report.status === 'RESOLVED' ? '✅ ÇÖZÜLDÜ' : '❌ REDDEDİLDİ'}
                    </div>
                  ) : (
                    <div className="fault-action-controls">
                      <div className="status-selector-mini">
                        {Object.entries(FAULT_STATUS_LABELS)
                          .filter(([val]) => !(report.operatorId && val === 'DISMISSED'))
                          .map(([val, lbl]) => (
                          <button
                            key={val}
                            className={`status-btn-mini ${val.toLowerCase()} ${(report.status === val || (report.status === 'OPEN' && val === 'PENDING')) ? 'active' : ''}`}
                            onClick={() => onUpdateFaultStatus(report.id, val)}
                          >
                            {lbl}
                          </button>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default FaultTab;
