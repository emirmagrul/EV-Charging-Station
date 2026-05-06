import React from 'react';
import StatusBadge from '../shared/StatusBadge';

const ChargerSelector = ({ chargers, selectedId, onSelect }) => {
  return (
    <div className="selection-group">
      <label>1. Şarj Ünitesini Seçin</label>
      <div className="charger-list">
        {chargers.map(c => (
          <div
            key={c.id}
            className={`charger-card ${selectedId === c.id ? 'selected' : ''}`}
            onClick={() => onSelect(c)}
          >
            <div className="power">{c.powerOutput} kW</div>
            <div className="type">{c.connectorType?.name || 'Bilinmiyor'}</div>
            <div style={{ marginTop: '8px' }}>
              <StatusBadge
                status={c.status}
                subtext={c.status === 'AVAILABLE' ? 'Şu an boşta' : c.status === 'OCCUPIED' ? 'Şu an şarjda' : 'Hizmet dışı'}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ChargerSelector;
