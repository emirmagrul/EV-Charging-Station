import React, { useState, useEffect } from 'react';
import './ActiveChargingSession.css';
import sessionService from '../../services/sessionService';

const ActiveChargingSession = ({ session, onSessionEnd }) => {
  const [currentPercentage, setCurrentPercentage] = useState(session.startPercentage || 15);
  const [elapsedTime, setElapsedTime] = useState(0);
  const [isEnding, setIsEnding] = useState(false);

  useEffect(() => {
    if (!session) return;

    // Başlangıç yüzdesi ataması
    if (session.startPercentage && currentPercentage === 15) {
      setCurrentPercentage(session.startPercentage);
    }

    // Geçen süreyi hesapla
    const updateElapsed = () => {
      const start = new Date(session.startTime).getTime();
      const now = new Date().getTime();
      setElapsedTime(Math.floor((now - start) / 1000)); // Saniye cinsinden
    };
    updateElapsed();

    // Zaman ve Yüzde simülasyonu
    const timer = setInterval(() => {
      updateElapsed();
      
      // Her saniyede çok ufak bir miktar artsın (Simülasyon)
      setCurrentPercentage(prev => {
        const next = prev + 0.05;
        return next > 100 ? 100 : next;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [session]);

  const handleEndSession = async () => {
    if (isEnding) return;
    try {
      setIsEnding(true);
      await sessionService.endSession(session.id);
      if (onSessionEnd) {
        onSessionEnd(); // Dashboard'a haber ver ki paneli kapatsın veya yenilesin
      }
    } catch (error) {
      console.error("Şarj bitirilemedi:", error);
      alert("Şarj bitirilirken bir hata oluştu.");
      setIsEnding(false);
    }
  };

  // %100 olunca otomatik bitir
  useEffect(() => {
    if (currentPercentage >= 100 && !isEnding) {
      handleEndSession();
    }
  }, [currentPercentage, isEnding]);

  if (!session) return null;

  // Çember için hesaplamalar
  const radius = 60;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (currentPercentage / 100) * circumference;

  // Zaman formatlama (MM:SS veya HH:MM:SS)
  const formatTime = (totalSeconds) => {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    
    if (hours > 0) {
      return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  return (
    <div className="active-session-container">
      <div className="circular-progress-wrapper">
        <svg className="circular-progress-svg" viewBox="0 0 140 140">
          <circle 
            className="progress-bg" 
            cx="70" cy="70" r={radius} 
          />
          <circle 
            className="progress-bar" 
            cx="70" cy="70" r={radius} 
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
          />
        </svg>
        <div className="progress-text">
          %{currentPercentage.toFixed(1)}
        </div>
      </div>

      <div className="session-info">
        <h3>Enerji Aktarımı Devam Ediyor</h3>
        <p>Araç şarj istasyonuna bağlı ve enerji alıyor.</p>
        <div className="time-elapsed">
          Geçen Süre: <span>{formatTime(elapsedTime)}</span>
        </div>
      </div>

      <button 
        className="end-session-btn" 
        onClick={handleEndSession}
        disabled={isEnding}
      >
        {isEnding ? 'Bitiriliyor...' : 'Şarjı Bitir'}
      </button>
    </div>
  );
};

export default ActiveChargingSession;
