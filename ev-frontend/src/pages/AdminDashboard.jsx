import React, { useState, useEffect } from 'react';
import adminService from '../services/adminService';
import './AdminDashboard.css';

import OverviewTab from './admin/OverviewTab';
import ReportingTab from './admin/ReportingTab';
import OptimizationTab from './admin/OptimizationTab';
import ConfigTab from './admin/ConfigTab';

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('overview'); // overview, reports, optimization, config
  const [revenueData, setRevenueData] = useState(null);
  const [activityData, setActivityData] = useState(null);
  const [healthData, setHealthData] = useState(null);
  const [peakHours, setPeakHours] = useState(null);
  const [reservations, setReservations] = useState([]);
  const [stations, setStations] = useState([]);
  const [configChanges, setConfigChanges] = useState({}); // Yerel düzenleme state'i
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updateStatus, setUpdateStatus] = useState({ id: null, loading: false });
  const [selectedStationForPeak, setSelectedStationForPeak] = useState('global');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    
    const requests = [
      { call: adminService.getRevenueReport, setter: setRevenueData, name: "Revenue" },
      { call: adminService.getUserActivity, setter: setActivityData, name: "Activity" },
      { call: adminService.getSystemHealth, setter: setHealthData, name: "Health" },
      { call: adminService.getPeakHours, setter: setPeakHours, name: "Peak" },
      { 
        call: adminService.getAllStations, 
        setter: (data) => {
          setStations(data);
          const initialChanges = {};
          data.forEach(s => {
            initialChanges[s.id] = { pricing: s.pricingPerKWh, hours: s.operatingHours };
          });
          setConfigChanges(initialChanges);
        }, 
        name: "Stations" 
      },
      { call: adminService.getAllReservations, setter: setReservations, name: "Reservations" }
    ];

    try {
      await Promise.all(requests.map(req => 
        req.call()
          .then(data => req.setter(data))
          .catch(err => console.error(`DEBUG: ${req.name} Fetch Failed:`, err))
      ));
    } catch (globalError) {
      console.error("Dashboard global fetch error:", globalError);
    } finally {
      setLoading(false);
    }
  };

  const handleStationPeakChange = async (stationId) => {
    setSelectedStationForPeak(stationId);
    try {
      const peak = stationId === 'global' 
        ? await adminService.getPeakHours() 
        : await adminService.getPeakHoursByStation(stationId);
      setPeakHours(peak);
    } catch (error) {
      console.error("Peak hour fetch failed:", error);
    }
  };

  const handleLocalChange = (id, field, value) => {
    setConfigChanges(prev => ({
      ...prev,
      [id]: { ...prev[id], [field]: value }
    }));
  };

  const handleConfigUpdate = async (id) => {
    const changes = configChanges[id];
    if (!changes) return;

    if (!window.confirm(`${id} ID'li istasyon için fiyatı ${changes.pricing} ₺ ve çalışma saatlerini ${changes.hours} olarak güncellemek istediğinize emin misiniz?`)) {
      return;
    }

    setUpdateStatus({ id, loading: true });
    try {
      await adminService.updateStationConfig(id, changes.pricing, changes.hours);
      setStations(prev => prev.map(s => s.id === id ? { ...s, pricingPerKWh: changes.pricing, operatingHours: changes.hours } : s));
      alert('Başarıyla güncellendi!');
    } catch (err) {
      alert('Güncelleme hatası: ' + err.message);
    } finally {
      setUpdateStatus({ id: null, loading: false });
    }
  };

  if (loading) {
    return (
      <div className="admin-dashboard">
        <div className="loading-state glass-panel">
          <div className="spinner"></div>
          <p>Sistem verileri ve ağ analizi yükleniyor...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-dashboard">
      <header className="admin-header">
        <div>
          <h1 className="text-gradient">Merkezi Yönetim Sistemi</h1>
          <p>Elektrikli Şarj Ağı Operasyon Kontrol Merkezi</p>
        </div>
        <button className="btn-outline" onClick={fetchDashboardData}>Verileri Güncelle</button>
      </header>

      <nav className="admin-tabs">
        <button className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>Genel Bakış</button>
        <button className={`tab-btn ${activeTab === 'reports' ? 'active' : ''}`} onClick={() => setActiveTab('reports')}>1. İdari Raporlama</button>
        <button className={`tab-btn ${activeTab === 'optimization' ? 'active' : ''}`} onClick={() => setActiveTab('optimization')}>2. Optimizasyon</button>
        <button className={`tab-btn ${activeTab === 'config' ? 'active' : ''}`} onClick={() => setActiveTab('config')}>3. Yapılandırma</button>
      </nav>

      <main className="tab-content">
        {activeTab === 'overview' && (
          <OverviewTab 
            revenueData={revenueData} 
            activityData={activityData} 
            healthData={healthData} 
            reservations={reservations} 
          />
        )}
        {activeTab === 'reports' && (
          <ReportingTab 
            stations={stations} 
            revenueData={revenueData} 
          />
        )}
        {activeTab === 'optimization' && (
          <OptimizationTab 
            stations={stations} 
            peakHours={peakHours} 
            revenueData={revenueData} 
            healthData={healthData} 
            selectedStationForPeak={selectedStationForPeak}
            onStationChange={handleStationPeakChange}
          />
        )}
        {activeTab === 'config' && (
          <ConfigTab 
            stations={stations} 
            configChanges={configChanges} 
            updateStatus={updateStatus} 
            handleLocalChange={handleLocalChange} 
            handleConfigUpdate={handleConfigUpdate} 
          />
        )}
      </main>
    </div>
  );
};

export default AdminDashboard;
