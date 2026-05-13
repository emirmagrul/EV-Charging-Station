import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import vehicleService from '../services/vehicleService';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import './AddVehicle.css';

const AddVehicle = () => {
  const { user, vehicles, setVehicles } = useAuth();
  const { setShowVehiclesModal } = useUI();
  const navigate = useNavigate();
  
  const [connectorTypes, setConnectorTypes] = useState([
    { id: 't2', name: 'Type 2 (AC - 22kW)' },
    { id: 'ccs', name: 'CCS (DC - 150kW)' },
    { id: 'cha', name: 'CHAdeMO (DC - 50kW)' },
    { id: 'ts', name: 'Tesla Supercharger (DC - 250kW)' }
  ]);

  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    brand: '',
    model: '',
    plateNumber: '',
    batteryCapacity: 75, // Backend DTO için zorunlu alan
    connectorTypeId: '',
    driverId: user?.id || ''
  });

  useEffect(() => {
    setShowVehiclesModal(false);
    
    const syncWithBackend = async () => {
      try {
        let backendData = await vehicleService.getAllConnectorTypes();
        
        if (!backendData || backendData.length === 0) {
          const defaults = [
            { name: 'Type 2 (AC - 22kW)' },
            { name: 'CCS (DC - 150kW)' },
            { name: 'CHAdeMO (DC - 50kW)' },
            { name: 'Tesla Supercharger (DC - 250kW)' }
          ];
          for (const item of defaults) {
            try { await vehicleService.registerConnectorType(item); } catch(e) {}
          }
          backendData = await vehicleService.getAllConnectorTypes();
        }
        
        if (backendData && backendData.length > 0) {
          setConnectorTypes(backendData);
        }
      } catch (err) {
        console.warn("Backend senkronizasyonu başarısız, yerel liste kullanılacak.");
      }
    };
    syncWithBackend();
  }, [setShowVehiclesModal]);

  const handleChange = (e) => {
    const value = e.target.type === 'number' ? parseFloat(e.target.value) : e.target.value;
    setFormData({ ...formData, [e.target.name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    let finalConnectorId = formData.connectorTypeId;
    const selectedType = connectorTypes.find(ct => ct.id === finalConnectorId);
    
    // Eğer ID sayı değilse (backend'e henüz senkronize edilmemişse), önce tipi kaydedelim
    if (isNaN(finalConnectorId)) {
       try {
         const created = await vehicleService.registerConnectorType({ name: selectedType.name });
         finalConnectorId = created.id;
       } catch (e) {
         return alert("Soket tipi senkronizasyon hatası.");
       }
    }

    setLoading(true);
    try {
      // Backend VehicleDto ile tam uyumlu payload
      const payload = {
        brand: formData.brand,
        model: formData.model,
        batteryCapacity: formData.batteryCapacity,
        plateNumber: formData.plateNumber,
        connectorTypeId: Number(finalConnectorId),
        driverId: Number(user?.id)
      };
      
      const newVehicle = await vehicleService.registerVehicle(payload);
      setVehicles([...vehicles, newVehicle]);
      alert("Araç başarıyla kaydedildi!");
      navigate('/dashboard');
    } catch (error) {
      console.error("Araç kayıt hatası:", error);
      alert(error.message || "Araç kaydedilemedi. Backend tarafında sürücü veya soket tipi bulunamamış olabilir.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="add-vehicle-container">
      <div className="glass-panel form-card">
        <button className="btn-back" onClick={() => navigate(-1)}>← Geri</button>
        <h2>Yeni Araç Ekle</h2>
        <p className="subtitle">Aracınızın bilgilerini girerek şarj işlemlerini kolaylaştırın.</p>

        <form onSubmit={handleSubmit} className="modern-form">
          <div className="form-group">
            <label>Marka</label>
            <input type="text" name="brand" placeholder="Örn: Tesla, Togg, BMW" value={formData.brand} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Model</label>
            <input type="text" name="model" placeholder="Örn: Model 3, T10X, i4" value={formData.model} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Batarya Kapasitesi (kWh)</label>
            <div className="number-input-wrapper">
              <button 
                type="button" 
                className="number-btn" 
                onClick={() => setFormData(prev => ({ ...prev, batteryCapacity: Math.max(1, prev.batteryCapacity - 1) }))}
              >
                −
              </button>
              <input 
                type="number" 
                name="batteryCapacity" 
                value={formData.batteryCapacity} 
                onChange={handleChange} 
                min="1"
                required 
              />
              <button 
                type="button" 
                className="number-btn" 
                onClick={() => setFormData(prev => ({ ...prev, batteryCapacity: prev.batteryCapacity + 1 }))}
              >
                +
              </button>
            </div>
          </div>

          <div className="form-group">
            <label>Plaka</label>
            <input type="text" name="plateNumber" placeholder="Örn: 34ABC123" value={formData.plateNumber} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Şarj Soket Tipi</label>
            <select name="connectorTypeId" value={formData.connectorTypeId} onChange={handleChange} required >
              <option value="">Seçiniz...</option>
              {connectorTypes.map(ct => (
                <option key={ct.id} value={ct.id}>{ct.name}</option>
              ))}
            </select>
          </div>

          <button type="submit" className="btn-primary-new btn-full" disabled={loading}>
            {loading ? 'Kaydediliyor...' : 'Aracı Kaydet'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AddVehicle;
