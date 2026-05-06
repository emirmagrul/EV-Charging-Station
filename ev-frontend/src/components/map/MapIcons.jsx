import L from 'leaflet';

export const userIcon = L.divIcon({
  html: `<div class="user-circle-marker"></div>`,
  className: '',
  iconSize: [16, 16],
  iconAnchor: [8, 8]
});

export const getPinIcon = (status) => {
  let color = '#10b981';
  if (status === 'OCCUPIED') color = '#f59e0b';
  if (status === 'OFFLINE') color = '#ef4444';

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
