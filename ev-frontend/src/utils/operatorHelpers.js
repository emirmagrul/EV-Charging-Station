import L from 'leaflet';

export const STATUS_LABELS = {
  AVAILABLE: { label: 'Müsait', color: 'green' },
  CHARGING: { label: 'Şarjda', color: 'yellow' },
  OUT_OF_SERVICE: { label: 'Hizmet Dışı', color: 'red' },
  OFFLINE: { label: 'Çevrimdışı', color: 'gray' },
};

export const FAULT_STATUS_LABELS = {
  PENDING: 'Beklemede',
  IN_PROGRESS: 'İşlemde',
  RESOLVED: 'Çözüldü',
  DISMISSED: 'Reddedildi',
};

export const RES_STATUS_LABELS = {
  PENDING: 'Bekliyor',
  CONFIRMED: 'Onaylandı',
  CANCELLED: 'İptal',
  CANCELLED_BY_OPERATOR: 'Operatör İptali',
  COMPLETED: 'Tamamlandı',
};

export const getPinIcon = (status) => {
  let color = '#10b981'; // Green
  if (status === 'CHARGING' || status === 'OCCUPIED') color = '#f59e0b'; // Yellow
  if (status === 'OUT_OF_SERVICE' || status === 'OFFLINE') color = '#ef4444'; // Red

  return L.divIcon({
    html: `<svg width="30" height="42" viewBox="0 0 30 42" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 0C6.71573 0 0 6.71573 0 15C0 26.25 15 42 15 42C15 42 30 26.25 30 15C30 6.71573 23.2843 0 15 0ZM15 20.25C12.1005 20.25 9.75 17.8995 9.75 15C9.75 12.1005 12.1005 9.75 15 9.75C17.8995 9.75 20.25 12.1005 20.25 15C20.25 17.8995 17.8995 20.25 15 20.25Z" fill="${color}"/>
            <circle cx="15" cy="15" r="5" fill="white"/>
           </svg>`,
    className: 'pin-marker',
    iconSize: [30, 42],
    iconAnchor: [15, 42],
    popupAnchor: [0, -40]
  });
};

export function formatDate(str) {
  if (!str) return '-';
  return new Date(str).toLocaleString('tr-TR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export function formatResDate(dateStr, timeStr) {
  if (!dateStr || !timeStr) return '-';
  try {
    const combined = `${dateStr}T${timeStr}`;
    return new Date(combined).toLocaleString('tr-TR', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });
  } catch (e) {
    return timeStr;
  }
}
