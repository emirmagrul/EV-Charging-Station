import React from 'react';

const StationShelf = ({ title, stations, onStationClick, badge, icon, actionButton }) => {
  if (!stations || stations.length === 0) return null;

  return (
    <section className="quick-access-section">
      <div className="section-title-row">
        <h2>{title}</h2>
        {actionButton}
      </div>
      <div className="horizontal-scroll">
        {stations.map(st => (
          <div key={st.id} className="recommend-card glass-panel" onClick={() => onStationClick(st)}>
            {badge && <div className="rec-badge">{badge}</div>}
            <div className="rec-info">
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                {icon && <span>{icon}</span>}
                <h3>{st.stationName}</h3>
              </div>
              <p>{st.address.length > 35 ? st.address.substring(0, 35) + '...' : st.address}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};

export default StationShelf;
