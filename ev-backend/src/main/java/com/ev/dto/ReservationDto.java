package com.ev.dto;

import com.ev.model.enums.ReservationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationDto {
    private Long id;
    private LocalDate reservationDate;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;
    
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;
    
    private ReservationStatus status;
    private Long driverId;
    private Long chargerId;
    private String stationName;
    private String driverName;
    private boolean reported;
}
