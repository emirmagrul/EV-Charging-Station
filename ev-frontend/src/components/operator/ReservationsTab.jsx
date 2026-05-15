import React, { useState } from 'react';
import { RES_STATUS_LABELS, formatResDate } from '../../utils/operatorHelpers';

function ReservationsTab({ reservations, loadingRes, onCancelReservation }) {
  const [filterStatus, setFilterStatus] = useState('ACTIVE');

  const safeReservations = Array.isArray(reservations) ? reservations : [];
  const filtered = filterStatus === 'ACTIVE'
    ? safeReservations.filter(r => r.status === 'PENDING' || r.status === 'CONFIRMED')
    : safeReservations;

  return (
    <div className="reservations-panel">
      <div className="fault-filter-bar">
        <button
          className={`filter-chip ${filterStatus === 'ACTIVE' ? 'active' : ''}`}
          onClick={() => setFilterStatus('ACTIVE')}
        >
          Aktif Rezervasyonlar
        </button>
        <button
          className={`filter-chip ${filterStatus === 'ALL' ? 'active' : ''}`}
          onClick={() => setFilterStatus('ALL')}
        >
          Tümü
        </button>
      </div>

      {loadingRes ? (
        <div className="op-loading"><div className="spin" /><span>Rezervasyonlar yükleniyor…</span></div>
      ) : filtered.length === 0 ? (
        <div className="op-empty-state">
          <div className="empty-icon">📅</div>
          <p>Gösterilecek rezervasyon bulunamadı.</p>
        </div>
      ) : (
        <div className="res-table-wrapper">
          <table className="res-table">
            <thead>
              <tr>
                <th>#ID</th>
                <th>Sürücü</th>
                <th>Şarj Ünitesi</th>
                <th>Başlangıç</th>
                <th>Bitiş</th>
                <th>Durum</th>
                <th>İşlem</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(r => (
                <tr key={r.id}>
                  <td style={{ color: 'var(--text-muted)', fontWeight: 600 }}>#{r.id}</td>
                  <td>{r.driverName || `Sürücü #${r.driverId}`}</td>
                  <td>Ünite #{r.chargerId}</td>
                  <td>{formatResDate(r.reservationDate, r.startTime)}</td>
                  <td>{formatResDate(r.reservationDate, r.endTime)}</td>
                  <td>
                    <span className={`res-status-badge res-status-${r.status}`}>
                      {RES_STATUS_LABELS[r.status] || r.status}
                    </span>
                  </td>
                  <td>
                    <button
                      className="cancel-res-btn"
                      disabled={r.status === 'CANCELLED' || r.status === 'COMPLETED' || r.status === 'CANCELLED_BY_OPERATOR'}
                      onClick={() => onCancelReservation(r)}
                    >
                      İptal Et
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default ReservationsTab;
