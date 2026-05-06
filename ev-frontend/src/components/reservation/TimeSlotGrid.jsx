import React from 'react';

const TimeSlotGrid = ({ slots, selectedTime, onSelect }) => {
  return (
    <div className="selection-group">
      <label>3. Başlangıç Saatini Seçin</label>
      <div className="time-slots">
        {slots.map(slot => (
          <div
            key={slot.time}
            className={`time-slot ${slot.disabled ? 'disabled' : ''} ${selectedTime === slot.time ? 'selected' : ''}`}
            onClick={() => {
              if (!slot.disabled) {
                onSelect(slot.time);
              }
            }}
          >
            {slot.time}
          </div>
        ))}
      </div>
    </div>
  );
};

export default TimeSlotGrid;
