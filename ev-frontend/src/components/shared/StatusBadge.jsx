import React from 'react';

const StatusBadge = ({ status, text, subtext }) => {
  const getStatusConfig = () => {
    switch (status) {
      case 'AVAILABLE':
        return { color: 'var(--primary)', label: text || 'MÜSAİT', icon: '✅' };
      case 'OCCUPIED':
        return { color: 'orange', label: text || 'DOLU', icon: '🟠' };
      case 'OFFLINE':
        return { color: '#ef4444', label: text || 'BAKIMDA', icon: '🔴' };
      default:
        return { color: '#888', label: text || 'BİLİNMİYOR', icon: '⚪' };
    }
  };

  const config = getStatusConfig();

  return (
    <div className="status-badge-container">
      <div style={{ color: config.color, fontSize: '0.85rem', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '4px' }}>
        <span>{config.icon}</span>
        <span>{config.label}</span>
      </div>
      {subtext && <div style={{ fontSize: '0.7rem', color: '#666', marginTop: '2px' }}>{subtext}</div>}
    </div>
  );
};

export default StatusBadge;
