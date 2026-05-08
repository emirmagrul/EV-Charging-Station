import React, { useState } from 'react';
import { formatTime } from '../../utils/formatters';
import reservationService from '../../services/reservationService';
import operatorService from '../../services/operatorService';

const ReservationsModal = ({ isOpen, onClose, reservations, setAllReservations, refreshUser }) => {
  const [resTab, setResTab] = useState('active'); // 'active' or 'history'
  const [reportingRes, setReportingRes] = useState(null); // Geri bildirim verilen rezervasyon
  const [reportDesc, setReportDesc] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [reportedResIds, setReportedResIds] = useState(new Set());

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

  const handleReportSubmit = async () => {
    if (!reportDesc.trim()) return alert("Lütfen bir açıklama girin.");
    setIsSubmitting(true);
    try {
      await operatorService.createFaultReport({
        chargerId: reportingRes.chargerId,
        description: reportDesc,
        priority: 'MEDIUM',
        driverId: reportingRes.driverId || reportingRes.userId
      });
      
      // Bu rezervasyonun raporlandığını kaydet
      setReportedResIds(prev => new Set(prev).add(reportingRes.id));
      
      alert("Geri bildiriminiz başarıyla iletildi. Teşekkür ederiz!");
      setReportingRes(null);
      setReportDesc('');
    } catch (e) {
      alert("Rapor gönderilemedi. Lütfen daha sonra tekrar deneyin.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="payment-modal" style={{ maxWidth: '600px', position: 'relative' }} onClick={e => e.stopPropagation()}>
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
                      background: isConfirmed ? 'rgba(0,255,0,0.1)' : isPending ? 'rgba(255,165,0,0.1)' : isCompleted ? 'rgba(16,185,129,0.1)' : 'rgba(255,0,0,0.1)',
                      color: isConfirmed ? '#0f0' : isPending ? 'orange' : isCompleted ? 'var(--success)' : '#ef4444'
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

                  {res.status === 'CANCELLED_BY_OPERATOR' && res.cancellationReason && (
                    <div style={{
                      marginTop: '12px', padding: '12px', 
                      background: 'rgba(239, 68, 68, 0.08)', 
                      borderLeft: '3px solid var(--danger)',
                      borderRadius: '4px 8px 8px 4px'
                    }}>
                      <span style={{ display: 'block', fontSize: '0.7rem', fontWeight: '800', color: 'var(--danger)', marginBottom: '4px', textTransform: 'uppercase' }}>
                        İptal Gerekçesi (Operatör):
                      </span>
                      <p style={{ margin: 0, fontSize: '0.82rem', color: 'rgba(255,255,255,0.8)', fontStyle: 'italic' }}>
                        "{res.cancellationReason}"
                      </p>
                    </div>
                  )}

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

                  {isCompleted && (
                    <button
                      onClick={() => !reportedResIds.has(res.id) && setReportingRes(res)}
                      disabled={reportedResIds.has(res.id)}
                      style={{
                        marginTop: '10px', width: '100%', padding: '10px',
                        background: reportedResIds.has(res.id) ? 'rgba(16,185,129,0.1)' : 'rgba(139,92,246,0.15)', 
                        border: reportedResIds.has(res.id) ? '1px solid rgba(16,185,129,0.3)' : '1px solid rgba(139,92,246,0.4)',
                        borderRadius: '10px', 
                        color: reportedResIds.has(res.id) ? 'var(--success)' : 'var(--accent)', 
                        cursor: reportedResIds.has(res.id) ? 'default' : 'pointer', 
                        fontSize: '0.85rem',
                        fontWeight: '700',
                        transition: 'all 0.3s ease',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px'
                      }}
                    >
                      {reportedResIds.has(res.id) ? (
                        <>✅ Geri Bildirim İletildi</>
                      ) : (
                        <>💬 Geri Bildirim Ver / Sorun Bildir</>
                      )}
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}

        {/* Geri Bildirim Alt Modalı - Premium Görünüm */}
        {reportingRes && (
          <div className="feedback-overlay-sub" style={{
            position: 'absolute', inset: 0, 
            background: 'rgba(15, 23, 42, 0.9)', 
            backdropFilter: 'blur(16px)',
            borderRadius: '18px', zIndex: 10, padding: '35px', 
            display: 'flex', flexDirection: 'column',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            boxShadow: '0 20px 50px rgba(0,0,0,0.5)',
            animation: 'fadeIn 0.3s ease-out'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '25px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ 
                  width: '40px', height: '40px', background: 'rgba(139, 92, 246, 0.2)', 
                  borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '20px', color: 'var(--accent)'
                }}>💬</div>
                <div>
                  <h4 style={{ margin: 0, fontSize: '1.2rem', fontWeight: '700' }}>Geri Bildirim</h4>
                  <span style={{ fontSize: '0.8rem', color: 'var(--accent)', fontWeight: '600' }}>{reportingRes.stationName}</span>
                </div>
              </div>
              <button className="btn-text" onClick={() => setReportingRes(null)} style={{ fontSize: '20px' }}>✕</button>
            </div>

            <div style={{ marginBottom: '20px' }}>
              <p style={{ fontSize: '0.9rem', color: 'rgba(255,255,255,0.7)', lineHeight: '1.6' }}>
                Deneyiminizi bizimle paylaşın. Bildiriminiz doğrudan operasyon ekibimize iletilecek ve hizmet kalitemizi artırmamıza yardımcı olacaktır.
              </p>
            </div>

            <div style={{ position: 'relative', flex: 1, marginBottom: '25px' }}>
              <textarea
                style={{
                  width: '100%', height: '100%',
                  background: 'rgba(255,255,255,0.03)', 
                  border: '1px solid rgba(255,255,255,0.1)',
                  borderRadius: '16px', padding: '18px', 
                  color: '#fff', fontSize: '0.95rem', outline: 'none',
                  resize: 'none', transition: 'all 0.3s ease',
                  fontFamily: 'inherit'
                }}
                className="feedback-textarea"
                placeholder="Buraya yazın..."
                value={reportDesc}
                onChange={(e) => setReportDesc(e.target.value)}
              />
              <div style={{ 
                position: 'absolute', bottom: '15px', right: '15px', 
                fontSize: '0.75rem', color: 'rgba(255,255,255,0.3)' 
              }}>
                {reportDesc.length} karakter
              </div>
            </div>

            <button
              className="btn-primary"
              disabled={isSubmitting || !reportDesc.trim()}
              onClick={handleReportSubmit}
              style={{ 
                width: '100%', padding: '15px', borderRadius: '14px',
                fontSize: '1rem', fontWeight: '700', letterSpacing: '0.5px',
                background: isSubmitting ? 'rgba(255,255,255,0.1)' : 'linear-gradient(135deg, var(--accent), var(--secondary))',
                boxShadow: isSubmitting ? 'none' : '0 10px 20px rgba(139, 92, 246, 0.3)',
                transition: 'all 0.3s ease',
                display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px'
              }}
            >
              {isSubmitting ? (
                <>
                  <div className="spin" style={{ width: '18px', height: '18px' }}></div>
                  Gönderiliyor...
                </>
              ) : (
                <>🚀 Bildirimi İlet</>
              )}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ReservationsModal;
