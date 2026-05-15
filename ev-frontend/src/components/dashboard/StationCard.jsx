import React from 'react';

const StationCard = ({ station, isFavorite, onToggleFavorite, onReserve, onDetail, onRoute, showFavoriteBtn = true }) => {
  return (
    <div className="glass-panel premium-card">
      <div className="card-top-icon">⚡</div>
      {showFavoriteBtn && (
        <div 
          className={`card-fav-btn ${isFavorite ? 'active' : ''}`}
          onClick={(e) => {
            e.stopPropagation();
            onToggleFavorite(station);
          }}
        >
          {isFavorite ? '❤️' : '🤍'}
        </div>
      )}
      <h3>{station.stationName}</h3>
      <p className="address-text">📍 {station.address}</p>
      <div className="premium-card-footer">
        <button className="btn-primary-new" onClick={() => onReserve(station.id)}>
          ⚡ Rezervasyon Yap
        </button>
        <div className="card-secondary-actions">
          <button className="btn-outline-mini" onClick={() => onDetail(station.id)}>Detay</button>
          <button className="btn-outline-mini" onClick={() => onRoute(station)}>Yol Tarifi</button>
        </div>
      </div>
    </div>
  );
};

export default StationCard;
