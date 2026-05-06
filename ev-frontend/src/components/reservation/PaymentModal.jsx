import React from 'react';

const PaymentModal = ({ isOpen, cost, balance, onConfirm, onCancel, processing }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="payment-modal">
        <h3>Ödeme Onayı</h3>
        <p>Cüzdanınızdan <strong>{cost} TL</strong> tahsil edilecektir. Onaylıyor musunuz?</p>
        <p style={{ fontSize: '0.9rem', color: '#aaa' }}>Mevcut Bakiyeniz: {balance} TL</p>

        <div className="modal-actions">
          <button className="btn-outline-new btn-full" onClick={onCancel} disabled={processing}>İptal</button>
          <button className="btn-primary-new btn-full" onClick={onConfirm} disabled={processing}>
            {processing ? 'İşleniyor...' : 'Ödemeyi Tamamla'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentModal;
