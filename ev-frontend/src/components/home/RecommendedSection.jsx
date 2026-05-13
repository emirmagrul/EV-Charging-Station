import React from 'react';

const RecommendedSection = ({ recommended, handleReservation, startInternalRouting, navigate }) => {
  if (!recommended || recommended.length === 0) return null;

  return (
    <section className="recommended-section">
      <div className="section-title-wrapper">
        <h2 className="section-title">Sana Özel Önerilenler</h2>
        <div className="title-line"></div>
      </div>
      <div className="recommended-scroll">
        {recommended.map(st => (
          <div key={st.id} className="glass-panel recommended-card">
            <div className="rec-badge">Yakınında</div>
            <h3>{st.stationName}</h3>
            <div className="card-actions-row vertical">
              <button className="btn-primary-new" onClick={() => handleReservation(st.id)}>Rezervasyon Yap</button>
              <div className="card-secondary-actions">
                <button className="btn-outline-mini" onClick={() => navigate(`/stations/${st.id}`)}>Detay</button>
                <button className="btn-outline-mini" onClick={() => startInternalRouting(st)}>Rota</button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};

export default RecommendedSection;
