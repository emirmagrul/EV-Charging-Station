import React, { useState, useEffect } from 'react';

export function Toast({ toast, onClose }) {
  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(onClose, 3500);
    return () => clearTimeout(t);
  }, [toast, onClose]);

  if (!toast) return null;

  return (
    <div className={`op-toast ${toast.type}`}>
      <span>{toast.type === 'success' ? '✅' : toast.type === 'error' ? '❌' : 'ℹ️'}</span>
      <span>{toast.message}</span>
    </div>
  );
}

export function ConfirmModal({ modal, onConfirm, onCancel }) {
  if (!modal) return null;
  return (
    <div className="op-modal-overlay" onClick={onCancel}>
      <div className="op-modal" onClick={e => e.stopPropagation()}>
        <h3>{modal.title}</h3>
        <p>{modal.description}</p>
        {modal.content}
        <div className="op-modal-actions">
          <button className="op-modal-cancel-btn" onClick={onCancel}>Vazgeç</button>
          <button className="op-modal-confirm-btn" onClick={onConfirm}>Onayla</button>
        </div>
      </div>
    </div>
  );
}

export function FaultModal({ modal, onConfirm, onCancel }) {
  const [description, setDescription] = useState('');
  if (!modal) return null;

  return (
    <div className="op-modal-overlay" onClick={onCancel}>
      <div className="op-modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header-icon red">⚠️</div>
        <h3>Arıza Bildirimi: Ünite #{modal.charger.id}</h3>
        <p>Üniteyi devre dışı bırakmadan önce lütfen arıza detaylarını belirtin.</p>
        
        <textarea 
          className="op-fault-textarea glass-panel"
          placeholder="Arıza açıklaması giriniz (örn: soket arızalı, bağlantı kopuk)..."
          value={description}
          onChange={e => setDescription(e.target.value)}
          autoFocus
        />

        <div className="op-modal-actions">
          <button className="op-modal-cancel-btn" onClick={onCancel}>Vazgeç</button>
          <button 
            className="op-modal-confirm-btn" 
            onClick={() => onConfirm(description)}
            disabled={!description.trim()}
          >
            Raporla ve Kapat
          </button>
        </div>
      </div>
    </div>
  );
}
