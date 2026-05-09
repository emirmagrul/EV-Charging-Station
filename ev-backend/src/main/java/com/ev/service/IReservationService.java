package com.ev.service;

import com.ev.dto.ReservationDto;

import java.util.List;

public interface IReservationService {
    ReservationDto makeReservation(ReservationDto reservationDto);
    void confirmReservation(Long reservationId);
    void cancelReservation(Long reservationId, String reason);
    List<ReservationDto> getMyReservations(Long driverId);
    List<ReservationDto> getBookedSlots(Long chargerId, java.time.LocalDate date);
    List<ReservationDto> findAll();
    List<ReservationDto> getByChargerId(Long chargerId);
}