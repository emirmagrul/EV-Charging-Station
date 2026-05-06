import React from 'react';

const UserStats = ({ user, addBalance, activeCount, onShowReservations }) => {
  const handleAddBalance = async () => {
    const amount = prompt("Yüklenecek tutarı girin (TL):");
    if (amount && !isNaN(amount) && Number(amount) > 0) {
      await addBalance(Number(amount));
    }
  };

  return (
    <div className="user-stats glass-panel">
      <div className="stat-item">
        <span className="stat-label">Cüzdan</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span className="stat-value">{user?.walletBalance ? user.walletBalance.toFixed(2) : '0.00'} TL</span>
          <button
            className="btn-mini-primary"
            style={{ padding: '4px 8px', fontSize: '0.8rem' }}
            onClick={handleAddBalance}
          >
            + Yükle
          </button>
        </div>
      </div>
      <div className="stat-divider"></div>

      <div className="stat-item" style={{ cursor: 'pointer' }} onClick={onShowReservations}>
        <span className="stat-label">
          Aktif Rezervasyon <span style={{ fontSize: '0.8rem', textDecoration: 'underline' }}>(Tıkla)</span>
        </span>
        {activeCount > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span className="stat-value" style={{ color: 'var(--primary)' }}>
              {activeCount} Adet
            </span>
          </div>
        ) : (
          <span className="stat-value">Yok</span>
        )}
      </div>
    </div>
  );
};

export default UserStats;
