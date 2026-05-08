import React from 'react';

function KpiGrid({ chargers, faultReports, reservations }) {
  const safeChargers = Array.isArray(chargers) ? chargers : [];
  const safeFaultReports = Array.isArray(faultReports) ? faultReports : [];
  const safeReservations = Array.isArray(reservations) ? reservations : [];

  const available = safeChargers.filter(c => c.status === 'AVAILABLE').length;
  const charging = safeChargers.filter(c => c.status === 'CHARGING').length;
  const outOfSvc = safeChargers.filter(c => c.status === 'OUT_OF_SERVICE' || c.status === 'OFFLINE').length;
  const openFaults = safeFaultReports.filter(f => f.status === 'OPEN').length;
  const activeRes = safeReservations.filter(r => r.status === 'PENDING' || r.status === 'CONFIRMED').length;

  const items = [
    { label: 'Müsait', value: available, colorClass: 'green', icon: '🟢' },
    { label: 'Şarjda', value: charging, colorClass: 'yellow', icon: '⚡' },
    { label: 'Hizmet Dışı', value: outOfSvc, colorClass: 'red', icon: '🔴' },
    { label: 'Açık Arızalar', value: openFaults, colorClass: 'purple', icon: '🛠️' },
    { label: 'Aktif Rez.', value: activeRes, colorClass: 'blue', icon: '📅' },
  ];

  return (
    <div className="operator-kpi-grid">
      {items.map(item => (
        <div key={item.label} className="kpi-card glass-panel">
          <div className={`kpi-icon ${item.colorClass}`}>{item.icon}</div>
          <div className="kpi-info">
            <span>{item.label}</span>
            <strong className={item.colorClass}>{item.value}</strong>
          </div>
        </div>
      ))}
    </div>
  );
}

export default KpiGrid;
