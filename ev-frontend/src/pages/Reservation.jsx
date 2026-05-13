import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import chargerService from '../services/chargerService';
import reservationService from '../services/reservationService';
import ChargerSelector from '../components/reservation/ChargerSelector';
import TimeSlotGrid from '../components/reservation/TimeSlotGrid';
import PaymentModal from '../components/reservation/PaymentModal';
import { formatTime } from '../utils/formatters';
import './Reservation.css';

const Reservation = () => {
  const { stationId } = useParams();
  const navigate = useNavigate();
  const { user, refreshUser } = useAuth();


  const [station, setStation] = useState(null);
  const [chargers, setChargers] = useState([]);
  const [selectedCharger, setSelectedCharger] = useState(null);
  
  // Date format: YYYY-MM-DD
  const getLocalDateString = (date) => {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const today = getLocalDateString(new Date());
  const tomorrowObj = new Date();
  tomorrowObj.setDate(tomorrowObj.getDate() + 1);
  const tomorrow = getLocalDateString(tomorrowObj);

  const [selectedDate, setSelectedDate] = useState(today);
  const [bookedSlots, setBookedSlots] = useState([]);
  
  const [selectedStartTime, setSelectedStartTime] = useState(null);
  const [durationHours, setDurationHours] = useState(1);

  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);

  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [pendingReservationId, setPendingReservationId] = useState(null);

  // 1. İstasyon ve Şarj Ünitelerini Yükle
  useEffect(() => {
    const fetchData = async () => {
      try {
        const st = await chargerService.getStationById(stationId);
        const ch = await chargerService.getChargersByStation(stationId);
        setStation(st);
        setChargers(ch);
        if (ch.length > 0) setSelectedCharger(ch[0]); // İlkini otomatik seç
      } catch (err) {
        alert("İstasyon verileri yüklenirken hata oluştu.");
        navigate('/dashboard');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [stationId, navigate]);

  // 2. Seçili Şarj Ünitesi veya Tarih değiştiğinde dolu saatleri getir
  useEffect(() => {
    if (!selectedCharger || !selectedDate) return;

    const fetchBookedSlots = async () => {
      try {
        const slots = await reservationService.getBookedSlots(selectedCharger.id, selectedDate);
        setBookedSlots(slots);
        setSelectedStartTime(null); // Seçimi sıfırla
      } catch (err) {
        console.error("Dolu saatler çekilemedi", err);
      }
    };
    fetchBookedSlots();
  }, [selectedCharger, selectedDate]);

  // Saat 00:00'dan 23:00'a kadar slot oluştur
  const generateTimeSlots = () => {
    const slots = [];
    const now = new Date();
    const nowHour = now.getHours();
    const nowMinutes = now.getMinutes();

    for (let i = 0; i < 24; i++) {
      const hourStr = i.toString().padStart(2, '0');
      const timeStr = `${hourStr}:00`;

      // ── BUGÜN: Şu andan en az 1 saat sonraki slotlara izin ver ──────────
      let isPast = false;
      if (selectedDate === today) {
        // Dakika bazlı kontrol: örn. 23:28'de sadece 23:00 değil, 00:00 da geçmiş
        if (i < nowHour || (i === nowHour && nowMinutes > 0)) {
          isPast = true; // Saat başlamış veya geçmiş
        }
        // Şu an 23:45 gibi ise, 23:00 slotu da artık başlamış sayılır
      }

      // ── YARIN: 24 saatten fazla ilerideki slotları engelle ───────────────
      // Şu an + 24 saat = deadline. Yarın o saatten ilerisini engelle.
      let isTooFar = false;
      if (selectedDate === tomorrow) {
        // Yarın saat i, şu andan kaç saat uzakta?
        // = (24 - nowHour) + i  (gece yarısı geçişi dahil)
        const hoursFromNow = (24 - nowHour - 1) + i + (nowMinutes > 0 ? 0 : 1);
        if (hoursFromNow >= 24) {
          isTooFar = true;
        }
      }

      // ── ÇALIŞMA SAATİ KONTROLÜ ──────────────────────────────────────────
      // "24 Saat", "24/7", "Tüm Gün" → 24 saat açık
      let isClosed = false;
      const is24h = !station?.operatingHours
        || station.operatingHours === "24/7"
        || station.operatingHours === "24 Saat"
        || station.operatingHours === "24 saat"
        || station.operatingHours.toLowerCase().includes("24");

      if (station && station.operatingHours && !is24h) {
        try {
          const [openStr, closeStr] = station.operatingHours.split("-");
          const openHour = parseInt(openStr.trim().split(":")[0]);
          const closeHour = parseInt(closeStr.trim().split(":")[0]);
          if (i < openHour || i >= closeHour) {
            isClosed = true;
          }
        } catch (e) {
          // Parse hatası → açık varsay
        }
      }

      // ── DOLULUK KONTROLÜ ────────────────────────────────────────────────
      let isBooked = false;
      const slotEnd = `${(i + 1).toString().padStart(2, '0')}:00`;
      for (const b of bookedSlots) {
        const bStart = formatTime(b.startTime);
        const bEnd   = formatTime(b.endTime);
        if (timeStr < bEnd && slotEnd > bStart) {
          isBooked = true;
          break;
        }
      }

      const isOffline = selectedCharger &&
        (selectedCharger.status === 'OFFLINE' || selectedCharger.status === 'OUT_OF_SERVICE');

      slots.push({
        time: timeStr,
        disabled: isPast || isTooFar || isBooked || isOffline || isClosed
      });
    }
    return slots;
  };


  const timeSlots = generateTimeSlots();

  // Maliyet Hesaplama
  const estimatedCost = () => {
    if (!station || !selectedCharger || !selectedStartTime) return 0;
    // Birim Fiyat * Güç(kW) * Saat
    return (station.pricingPerKWh * selectedCharger.powerOutput * durationHours).toFixed(2);
  };

  const getEndTime = () => {
    if (!selectedStartTime) return "";
    const startHour = parseInt(selectedStartTime.split(':')[0]);
    const endHour = startHour + durationHours;
    if (endHour >= 24) return "23:59";
    return `${endHour.toString().padStart(2, '0')}:00`;
  };

  const handleMakeReservation = async () => {
    if (!selectedStartTime) return alert("Lütfen bir başlangıç saati seçin.");
    
    // Bitiş saati hesapla
    const endTimeStr = getEndTime();

    // 2 saat seçildiyse, bir sonraki saatin de boş olduğunu doğrula
    if (durationHours === 2) {
      const startHour = parseInt(selectedStartTime.split(':')[0]);
      const nextHourSlot = timeSlots.find(s => s.time === `${(startHour + 1).toString().padStart(2, '0')}:00`);
      if (nextHourSlot && nextHourSlot.disabled) {
        return alert("Seçtiğiniz 2 saatlik dilimin ikinci saati dolu. Lütfen süreyi 1 saate indirin veya başka bir saat seçin.");
      }
    }

    setProcessing(true);
    try {
      const payload = {
        driverId: user.id,
        chargerId: selectedCharger.id,
        reservationDate: selectedDate,
        startTime: `${selectedStartTime}:00`, // HH:mm:ss
        endTime: `${endTimeStr}:00`
      };

      const res = await reservationService.makeReservation(payload);
      setPendingReservationId(res.id);
      setShowPaymentModal(true);
    } catch (err) {
      alert(err.message || "Rezervasyon oluşturulamadı.");
    } finally {
      setProcessing(false);
    }
  };

  const handleConfirmPayment = async () => {
    setProcessing(true);
    try {
      await reservationService.confirmReservation(pendingReservationId);
      await refreshUser();
      alert("Rezervasyon başarıyla onaylandı ve ödeme alındı!");
      navigate('/dashboard');
    } catch (err) {
      alert(err.message || "Ödeme başarısız oldu. Lütfen cüzdan bakiyenizi kontrol edin.");
    } finally {
      setProcessing(false);
      setShowPaymentModal(false);
    }
  };

  const handleCancelPayment = async () => {
    // Backend'de PENDING kalanlar cron ile temizlenecek, ama anında iptal edebiliriz veya sadece modalı kapatabiliriz.
    // Şimdilik sadece modalı kapatıp backend'i yormayalım (veya cancel servisini çağırabiliriz).
    try {
      await reservationService.cancelReservation(pendingReservationId);
    } catch(e) {}
    setShowPaymentModal(false);
    setPendingReservationId(null);
  };

  if (loading) return <div className="reservation-page"><h2>Yükleniyor...</h2></div>;

  return (
    <div className="reservation-page">
      <h2>Rezervasyon Oluştur</h2>

      <div className="reservation-container">
        {/* İstasyon Özeti */}
        <div className="station-info-card">
          <h2>⚡ {station.stationName}</h2>
          <p>📍 {station.address}</p>
          <p>💰 Birim Fiyat: <strong>{station.pricingPerKWh} TL / kWh</strong></p>
        </div>

        {/* Cihaz ve Zaman Seçimi */}
        <div className="reservation-panel">
          
          <ChargerSelector 
            chargers={chargers} 
            selectedId={selectedCharger?.id} 
            onSelect={setSelectedCharger} 
          />

          <div className="selection-group">
            <label>2. Tarih Seçin (En fazla 24 saat önceden)</label>
            <select 
              className="date-picker" 
              value={selectedDate} 
              onChange={(e) => setSelectedDate(e.target.value)}
            >
              <option value={today}>Bugün ({today})</option>
              <option value={tomorrow}>Yarın ({tomorrow})</option>
            </select>
          </div>

          <TimeSlotGrid 
            slots={timeSlots} 
            selectedTime={selectedStartTime} 
            onSelect={(time) => {
              setSelectedStartTime(time);
              if (time === '23:00' && durationHours === 2) {
                setDurationHours(1);
              }
            }} 
          />

          <div className="selection-group">
            <label>4. Süre Seçin (Maksimum 2 Saat)</label>
            <div style={{display:'flex', gap:'10px'}}>
              <button 
                className={`btn-outline-mini ${durationHours === 1 ? 'active' : ''}`} 
                onClick={() => setDurationHours(1)}
              >
                1 Saat
              </button>
              <button 
                className={`btn-outline-mini ${durationHours === 2 ? 'active' : ''}`} 
                onClick={() => setDurationHours(2)}
                disabled={selectedStartTime === '23:00'}
                title={selectedStartTime === '23:00' ? "Gece yarısını geçen rezervasyon yapılamaz" : ""}
              >
                2 Saat
              </button>
            </div>
          </div>

          {selectedStartTime && (
            <div className="summary-panel">
              <p><span>Tarih:</span> <span>{selectedDate}</span></p>
              <p><span>Zaman:</span> <span>{selectedStartTime} - {getEndTime()}</span></p>
              <p><span>Cihaz:</span> <span>{selectedCharger.powerOutput} kW ({selectedCharger.connectorType.name})</span></p>
              <p className="total"><span>Tahmini Tutar:</span> <span>{estimatedCost()} TL</span></p>
            </div>
          )}

          <div className="reservation-actions">
            <button className="btn-outline-new btn-full" onClick={() => navigate(-1)}>İptal Et</button>
            <button 
              className="btn-primary-new btn-full" 
              disabled={!selectedStartTime || processing || selectedCharger?.status === 'OFFLINE'}
              onClick={handleMakeReservation}
            >
              {processing ? 'İşleniyor...' : 'Devam Et'}
            </button>
          </div>

        </div>
      </div>

      {/* Ödeme / Onay Modalı */}
      <PaymentModal 
        isOpen={showPaymentModal}
        cost={estimatedCost()}
        balance={user?.walletBalance}
        onConfirm={handleConfirmPayment}
        onCancel={handleCancelPayment}
        processing={processing}
      />

    </div>
  );
};

export default Reservation;
