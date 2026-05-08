import React, { useState, useEffect, useCallback } from 'react';
import operatorService from '../services/operatorService';
import { useAuth } from '../context/AuthContext';

// Yeni ayrıştırılan bileşenler
import KpiGrid from '../components/operator/KpiGrid';
import StatusTab from '../components/operator/StatusTab';
import FaultTab from '../components/operator/FaultTab';
import ReservationsTab from '../components/operator/ReservationsTab';
import MapTab from '../components/operator/MapTab';
import { Toast, ConfirmModal, FaultModal } from '../components/operator/OperatorModals';

// Stil ve Leaflet
import './OperatorDashboard.css';
import 'leaflet/dist/leaflet.css';

const OperatorDashboard = () => {
  const [activeTab, setActiveTab] = useState('status');
  const { user } = useAuth();

  const [stations, setStations] = useState([]);
  const [chargers, setChargers] = useState([]);
  const [faultReports, setFaultReports] = useState([]);
  const [reservations, setReservations] = useState([]);

  const [loadingChargers, setLoadingChargers] = useState(false);
  const [loadingFaults, setLoadingFaults] = useState(false);
  const [loadingRes, setLoadingRes] = useState(false);

  const [toast, setToast] = useState(null);
  const [modal, setModal] = useState(null);   
  const [faultModal, setFaultModal] = useState(null); 

  const showToast = (message, type = 'success') => setToast({ message, type });
  const hideToast = useCallback(() => setToast(null), []);

  // ── Data loaders ─────────────────────────────────────────────────────────

  const loadAllChargers = async () => {
    if (!user?.id) return;
    setLoadingChargers(true);
    try {
      const stList = await operatorService.getStationsByOperator(user.id);
      setStations(stList);
      
      if (stList.length === 0) {
        setChargers([]);
        setLoadingChargers(false);
        return;
      }

      const all = await Promise.all(
        stList.map(async s => {
          try {
            const chargerList = await operatorService.getChargersByStation(s.id);
            return chargerList.map(c => ({ 
              ...c, 
              stationId: s.id, 
              stationName: s.stationName,
              status: c.status === 'OCCUPIED' ? 'CHARGING' : c.status 
            }));
          } catch (err) {
            return [];
          }
        })
      );
      setChargers(all.flat());
    } catch (e) { 
      showToast('Şarj üniteleri yüklenemedi.', 'error'); 
    }
    finally { setLoadingChargers(false); }
  };

  const loadFaultReports = async () => {
    setLoadingFaults(true);
    try {
      const data = await operatorService.getAllFaultReports();
      setFaultReports(data);
    } catch { showToast('Arıza raporları yüklenemedi.', 'error'); }
    finally { setLoadingFaults(false); }
  };

  const loadReservations = async () => {
    setLoadingRes(true);
    try {
      const data = await operatorService.getAllReservations();
      setReservations(data);
    } catch { showToast('Rezervasyonlar yüklenemedi.', 'error'); }
    finally { setLoadingRes(false); }
  };

  const refreshAll = () => {
    loadAllChargers();
    loadFaultReports();
    loadReservations();
    showToast('Veriler yenileniyor...', 'info');
  };

  useEffect(() => {
    loadAllChargers();
    loadFaultReports();
    loadReservations();
  }, [user?.id]);

  // ── Action handlers ───────────────────────────────────────────────────────

  const handleStatusChange = (charger, newStatus) => {
    const labels = { OUT_OF_SERVICE: 'Hizmet Dışı', AVAILABLE: 'Müsait', OFFLINE: 'Çevrimdışı' };

    if (newStatus === 'OUT_OF_SERVICE' || newStatus === 'OFFLINE') {
      setFaultModal({ charger, newStatus });
      return;
    }

    setModal({
      title: `Ünite #${charger.id} — ${labels[newStatus]}`,
      description: `Ünite #${charger.id} durumu "${labels[newStatus]}" olarak güncellenecek.`,
      onConfirm: async () => {
        setModal(null);
        try {
          await operatorService.updateChargerStatus(charger.id, newStatus);
          setChargers(prev => prev.map(c => c.id === charger.id ? { ...c, status: newStatus } : c));
          showToast(`Ünite #${charger.id} → ${labels[newStatus]}`, 'success');
        } catch (e) {
          showToast(e.message || 'Durum güncellenemedi.', 'error');
        }
      }
    });
  };

  const handleFaultSubmit = async (description) => {
    const { charger, newStatus } = faultModal;
    setFaultModal(null);
    try {
      await operatorService.createFaultReport({
        chargerId: charger.id,
        operatorId: user.id,
        description: description
      });

      await operatorService.updateChargerStatus(charger.id, newStatus);
      setChargers(prev => prev.map(c => c.id === charger.id ? { ...c, status: newStatus } : c));
      showToast(`Ünite #${charger.id} kapatıldı ve rapor oluşturuldu.`, 'success');
      loadFaultReports();
      loadReservations(); 
    } catch (e) {
      showToast(e.message || 'İşlem başarısız.', 'error');
    }
  };

  const handleUpdateFaultStatus = async (reportId, newStatus) => {
    try {
      await operatorService.updateFaultReportStatus(reportId, newStatus);
      
      // Eğer rapor çözüldüyse, ilgili üniteyi otomatik olarak "Müsait" yap
      if (newStatus === 'RESOLVED') {
        const report = faultReports.find(f => f.id === reportId);
        if (report) {
          await operatorService.updateChargerStatus(report.chargerId, 'AVAILABLE');
          setChargers(prev => prev.map(c => c.id === report.chargerId ? { ...c, status: 'AVAILABLE' } : c));
          showToast(`Ünite #${report.chargerId} otomatik olarak hizmete açıldı.`, 'success');
        }
      }

      setFaultReports(prev => prev.map(f => f.id === reportId ? { ...f, status: newStatus } : f));
      showToast('Rapor durumu güncellendi.', 'success');
    } catch (e) {
      showToast(e.message || 'Rapor güncellenemedi.', 'error');
    }
  };

  const handleDeactivateCharger = (chargerId, existingReport = null) => {
    const charger = chargers.find(c => c.id === chargerId);
    if (!charger) {
      showToast('Ünite bulunamadı.', 'error');
      return;
    }

    if (existingReport) {
      setModal({
        title: `Üniteyi Kapat — Ünite #${chargerId}`,
        description: `Bu üniteyi mevcut arıza raporuna istinaden hizmet dışı bırakmak istiyor musunuz?`,
        onConfirm: async () => {
          setModal(null);
          try {
            await operatorService.updateChargerStatus(chargerId, 'OUT_OF_SERVICE');
            setChargers(prev => prev.map(c => c.id === chargerId ? { ...c, status: 'OUT_OF_SERVICE' } : c));
            showToast(`Ünite #${chargerId} hizmet dışına alındı.`, 'success');
            // Raporu da otomatik olarak "İşlemde" yapalım
            handleUpdateFaultStatus(existingReport.id, 'IN_PROGRESS');
          } catch (e) {
            showToast('Ünite kapatılamadı.', 'error');
          }
        }
      });
    } else {
      handleStatusChange(charger, 'OUT_OF_SERVICE');
    }
  };

  const handleCancelReservation = (reservation) => {
    let cancelReason = '';
    setModal({
      title: `Rezervasyon İptali — #${reservation.id}`,
      description: `Bu rezervasyonu iptal etmek üzeresiniz. Lütfen sürücüye iletilecek iptal gerekçesini yazın:`,
      content: (
        <textarea
          className="op-fault-textarea"
          placeholder="Örn: Ünite arızası nedeniyle hizmet dışı..."
          onChange={(e) => cancelReason = e.target.value}
          style={{ marginTop: '15px', marginBottom: '0' }}
        />
      ),
      onConfirm: async () => {
        if (!cancelReason.trim()) {
          showToast('Lütfen bir iptal gerekçesi girin.', 'error');
          return;
        }
        setModal(null);
        try {
          await operatorService.cancelReservationByOperator(reservation.id, cancelReason);
          setReservations(prev => prev.map(r => r.id === reservation.id ? { ...r, status: 'CANCELLED_BY_OPERATOR', cancellationReason: cancelReason } : r));
          showToast('Rezervasyon operatör tarafından iptal edildi.', 'info');
        } catch (e) {
          showToast('İptal işlemi başarısız.', 'error');
        }
      }
    });
  };

  // ── Badge counts ──────────────────────────────────────────────────────────
  const openFaultCount = faultReports.filter(f => f.status === 'OPEN' || f.status === 'PENDING').length;
  const activeResCount = reservations.filter(r => r.status === 'PENDING' || r.status === 'CONFIRMED').length;

  return (
    <div className="operator-page">
      <div className="operator-header">
        <div className="operator-header-icon">🛡️</div>
        <div className="operator-header-text">
          <h1>Operatör Paneli</h1>
          <p>Şarj ünitelerini yönetin, arızaları takip edin ve rezervasyonları denetleyin. (ID: {user?.id || '?'})</p>
        </div>
        <button className="refresh-btn" style={{ marginLeft: 'auto' }} onClick={refreshAll}>
          🔄 Verileri Yenile
        </button>
      </div>

      <KpiGrid chargers={chargers} faultReports={faultReports} reservations={reservations} />

      <div className="operator-tabs">
        <button className={`op-tab ${activeTab === 'status' ? 'active' : ''}`} onClick={() => setActiveTab('status')}>
          🔌 Durum Yönetimi <span className="tab-badge">{chargers.length}</span>
        </button>
        <button className={`op-tab ${activeTab === 'faults' ? 'active' : ''}`} onClick={() => setActiveTab('faults')}>
          🛠️ Arıza Raporları {openFaultCount > 0 && <span className="tab-badge">{openFaultCount}</span>}
        </button>
        <button className={`op-tab ${activeTab === 'reservations' ? 'active' : ''}`} onClick={() => setActiveTab('reservations')}>
          📅 Rezervasyonlar {activeResCount > 0 && <span className="tab-badge">{activeResCount}</span>}
        </button>
        <button className={`op-tab ${activeTab === 'map' ? 'active' : ''}`} onClick={() => setActiveTab('map')}>
          🗺️ Harita Görünümü
        </button>
      </div>

      <div className="operator-tab-content">
        {activeTab === 'status' && (
          <StatusTab 
            stations={stations} 
            chargers={chargers} 
            loadingChargers={loadingChargers} 
            onStatusChange={handleStatusChange} 
          />
        )}
        {activeTab === 'faults' && (
          <FaultTab 
            faultReports={faultReports} 
            loadingFaults={loadingFaults} 
            onUpdateFaultStatus={handleUpdateFaultStatus}
            onDeactivateCharger={handleDeactivateCharger}
          />
        )}
        {activeTab === 'reservations' && (
          <ReservationsTab 
            reservations={reservations} 
            loadingRes={loadingRes} 
            onCancelReservation={handleCancelReservation} 
          />
        )}
        {activeTab === 'map' && <MapTab stations={stations} chargers={chargers} />}
      </div>

      <ConfirmModal modal={modal} onConfirm={modal?.onConfirm} onCancel={() => setModal(null)} />
      <FaultModal modal={faultModal} onConfirm={handleFaultSubmit} onCancel={() => setFaultModal(null)} />
      <Toast toast={toast} onClose={hideToast} />
    </div>
  );
};

export default OperatorDashboard;
