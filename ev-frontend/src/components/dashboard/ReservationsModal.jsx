import React, { useState } from 'react';
import { formatTime } from '../../utils/formatters';
import reservationService from '../../services/reservationService';

const ReservationsModal = ({ isOpen, onClose, reservations, setAllReservations, refreshUser }) => {
  const [resTab, setResTab] = useState('active'); // 'active' or 'history'

  if (!isOpen) return null;

  const filteredReservations = reservations
    .filter(r => resTab === 'active' 
      ? (r.status === 'PENDING' || r.status === 'CONFIRMED') 
      : (r.status !== 'PENDING' && r.status !== 'CONFIRMED'))
    .sort((a, b) => b.id - a.id);

  const handleCancel = async (res) => {
    if (!window.confirm('Bu rezervasyonu iptal etmek istediğinizden emin misiniz?')) return;
    try {
      await reservationService.cancelReservation(res.id);
      setAllReservations(prev => prev.map(r => r.id === res.id ? { ...r, status: 'CANCELLED' } : r));
      await refreshUser();
    } catch (e) {
      alert(e.message || 'İptal işlemi başarısız.');
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="payment-modal" style={{ maxWidth: '600px' }} onClick={e => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
          <h3 style={{ margin: 0 }}>Rezervasyonlarım</h3>
          <button className="btn-text" onClick={onClose}>✕</button>
        </div>

        <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
          <button
            className={resTab === 'active' ? 'btn-mini-primary' : 'btn-mini-outline'}
            onClick={() => setResTab('active')}
          >
            Aktif / Bekleyen
          </button>
          <button
            className={resTab === 'history' ? 'btn-mini-primary' : 'btn-mini-outline'}
            onClick={() => setResTab('history')}
          >
            Geçmiş
          </button>
        </div>

        {filteredReservations.length === 0 ? (
          <p>{resTab === 'active' ? 'Aktif veya bekleyen rezervasyonunuz bulunmuyor.' : 'Henüz geçmiş bir rezervasyonunuz yok.'}</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', maxHeight: '400px', overflowY: 'auto', textAlign: 'left' }}>
            {filteredReservations.map(res => {
              const startTimeStr = formatTime(res.startTime);
              const endTimeStr = formatTime(res.endTime);

              const isPending = res.status === 'PENDING';
              const isConfirmed = res.status === 'CONFIRMED';
              const isCompleted = res.status === 'COMPLETED';

              return (
                <div key={res.id} style={{ background: 'rgba(255,255,255,0.05)', padding: '15px', borderRadius: '10px', border: '1px solid rgba(255,255,255,0.1)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                    <strong style={{ color: 'var(--primary)', fontSize: '1.1rem' }}>{res.stationName}</strong>
                    <span style={{
                      padding: '3px 8px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold',
                      background: isConfirmed ? 'rgba(0,255,0,0.1)' : isPending ? 'rgba(255,165,0,0.1)' : isCompleted ? 'rgba(255,255,255,0.1)' : 'rgba(255,0,0,0.1)',
                      color: isConfirmed ? '#0f0' : isPending ? 'orange' : isCompleted ? '#ccc' : '#ef4444'
                    }}>
                      {res.status === 'CONFIRMED' ? 'ONAYLANDI' :
                        res.status === 'PENDING' ? 'ÖDEME BEKLİYOR' :
                        res.status === 'COMPLETED' ? 'TAMAMLANDI' :
                        res.status === 'CANCELLED' ? 'İPTAL EDİLDİ' : 'OPERATÖR İPTAL'}
                    </span>
                  </div>
                  <p style={{ margin: '5px 0', fontSize: '0.9rem' }}>📅 Tarih: {res.reservationDate}</p>
                  <p style={{ margin: '5px 0', fontSize: '0.9rem' }}>⏱️ Saat: {startTimeStr} - {endTimeStr}</p>
                  <p style={{ margin: '5px 0', fontSize: '0.8rem', color: '#888' }}>Referans ID: #{res.id}</p>

                  {(isPending || isConfirmed) && (
                    <button
                      onClick={() => handleCancel(res)}
                      style={{
                        marginTop: '10px', width: '100%', padding: '8px',
                        background: 'rgba(239,68,68,0.15)', border: '1px solid rgba(239,68,68,0.4)',
                        borderRadius: '8px', color: '#ef4444', cursor: 'pointer', fontSize: '0.85rem'
                      }}
                    >
                      🗑️ Rezervasyonu İptal Et
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default ReservationsModal;
