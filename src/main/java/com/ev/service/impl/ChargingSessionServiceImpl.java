package com.ev.service.impl;

import com.ev.dto.ChargingSessionDto;
import com.ev.model.ChargingSession;
import com.ev.model.Reservation;
import com.ev.repository.ChargingSessionRepository;
import com.ev.repository.ReservationRepository;
import com.ev.service.IChargingSessionService;
import com.ev.service.IEVDriverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChargingSessionServiceImpl implements IChargingSessionService {

    private final ChargingSessionRepository chargingSessionRepository;
    private final ReservationRepository reservationRepository;
    private final IEVDriverService evDriverService; //cüzdan ödemesi için

    @Override
    @Transactional
    public ChargingSessionDto startSession(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı!"));

        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStartTime(LocalDateTime.now());
        session.setStatus("ACTIVE");

        ChargingSession saved = chargingSessionRepository.save(session);

        ChargingSessionDto chargingSessionDto = new ChargingSessionDto();
        chargingSessionDto.setId(saved.getId());
        chargingSessionDto.setStartTime(saved.getStartTime());
        return chargingSessionDto;
    }

    @Override
    @Transactional
    public ChargingSessionDto endSession(Long sessionId, double energyConsumedKWh) {
        ChargingSession session = chargingSessionRepository
                .findById(sessionId).orElseThrow(() -> new RuntimeException("Seans bulunamadı!"));

        session.setEndTime(LocalDateTime.now());
        session.setEnergyConsumedKwh(energyConsumedKWh);
        session.setStatus("INACTIVE");

        //Maliyet Hesaplama
        BigDecimal unitPrice = session.getReservation().getCharger().getStation().getPricingPerKWh();
        BigDecimal totalCost = unitPrice.multiply(BigDecimal.valueOf(energyConsumedKWh));
        session.setTotalCost(totalCost);

        //bakiye güncelleme
        evDriverService.deductBalance(session.getReservation().getDriver().getId(), totalCost);

        chargingSessionRepository.save(session);

        ChargingSessionDto responseDto = new ChargingSessionDto();
        responseDto.setId(session.getId());
        responseDto.setEndTime(session.getEndTime());
        responseDto.setEnergyConsumedKwh(session.getEnergyConsumedKwh());
        responseDto.setTotalCost(session.getTotalCost());
        responseDto.setStatus(session.getStatus());
        responseDto.setReservationId(session.getReservation().getId());

        return responseDto;
    }
}
