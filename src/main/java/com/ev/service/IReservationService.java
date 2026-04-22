package com.ev.service;

import com.ev.dto.ReservationDto;

import java.util.List;

public interface IReservationService {
    ReservationDto makeReservation(ReservationDto reservationDto);
    void cancelReservation(Long reservationId);
    List<ReservationDto> getMyReservations(Long driverId);
}
