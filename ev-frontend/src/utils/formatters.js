/**
 * Backend'den gelen LocalTime (Array veya String) formatını "HH:mm" formatına dönüştürür.
 * @param {string|number[]} time 
 * @returns {string} "HH:mm"
 */
export const formatTime = (time) => {
  if (!time) return "00:00";
  
  if (typeof time === 'string') {
    return time.substring(0, 5);
  }
  
  if (Array.isArray(time)) {
    const hours = time[0].toString().padStart(2, '0');
    const minutes = (time[1] || 0).toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }
  
  return "00:00";
};

/**
 * Para birimini formatlar.
 */
export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('tr-TR', {
    style: 'currency',
    currency: 'TRY',
  }).format(amount || 0);
};
