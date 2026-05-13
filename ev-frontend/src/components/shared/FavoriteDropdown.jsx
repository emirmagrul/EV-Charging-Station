import React from 'react';

const FavoriteDropdown = ({ 
  favorites, 
  toggleFavorite, 
  setSelectedStation, 
  setShowFavoritesModal 
}) => {
  return (
    <div className="dropdown-panel glass-panel">
      <div className="dropdown-header">
        <span>Favori İstasyonlarım</span>
      </div>
      <div className="dropdown-body">
        {favorites.length > 0 ? (
          favorites.map(st => (
            <div 
              key={st.id} 
              className="dropdown-item clickable" 
              onClick={() => {
                setSelectedStation(st);
                setShowFavoritesModal(false);
              }}
            >
              <div className="item-info">
                <h4>{st.stationName}</h4>
                <p>{st.address.substring(0, 30)}...</p>
              </div>
              <button 
                className="remove-btn-mini" 
                onClick={(e) => {
                  e.stopPropagation(); 
                  toggleFavorite(st);
                }}
              >
                ✕
              </button>
            </div>
          ))
        ) : (
          <div className="empty-dropdown">Henüz favori istasyonun yok.</div>
        )}
      </div>
    </div>
  );
};

export default FavoriteDropdown;
