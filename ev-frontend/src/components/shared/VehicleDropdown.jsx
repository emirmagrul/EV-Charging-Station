import React from 'react';
import { useNavigate } from 'react-router-dom';

const VehicleDropdown = ({ 
  vehicles, 
  deleteVehicle, 
  selectedVehicle, 
  setSelectedVehicle, 
  setShowVehiclesModal 
}) => {
  const navigate = useNavigate();

  return (
    <div className="dropdown-panel glass-panel">
      <div className="dropdown-header">
        <span>Kayıtlı Araçlarım</span>
      </div>
      <div className="dropdown-body">
        {vehicles.length > 0 ? (
          vehicles.map(v => (
            <div 
              key={v.id} 
              className={`dropdown-item clickable ${selectedVehicle?.id === v.id ? 'selected' : ''}`}
              onClick={() => {
                setSelectedVehicle(selectedVehicle?.id === v.id ? null : v);
                setShowVehiclesModal(false);
              }}
            >
              <div className="item-icon">🚗</div>
              <div className="item-info">
                <h4>{v.brand} {v.model}</h4>
                <p>{v.plateNumber}</p>
              </div>
              <div className="item-actions">
                {selectedVehicle?.id === v.id && <div className="selected-badge">✓</div>}
                <button 
                  className="remove-btn-mini" 
                  onClick={(e) => {
                    e.stopPropagation();
                    if(window.confirm(`${v.brand} ${v.model} aracını silmek istediğine emin misin?`)) {
                      deleteVehicle(v.id);
                      if(selectedVehicle?.id === v.id) setSelectedVehicle(null);
                    }
                  }}
                >
                  ✕
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-dropdown">Henüz araç eklemediniz.</div>
        )}
        <button 
          className="btn-primary-new btn-full" 
          onClick={() => {
            navigate('/vehicles/add');
            setShowVehiclesModal(false);
          }}
        >
          + Araç Ekle
        </button>
      </div>
    </div>
  );
};

export default VehicleDropdown;
