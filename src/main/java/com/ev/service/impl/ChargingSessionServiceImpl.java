package com.ev.service.impl;

import com.ev.dto.ChargingSessionDto;
import com.ev.model.ChargingSession;
import com.ev.model.Reservation;
import com.ev.model.enums.ReservationStatus;
import com.ev.model.enums.SessionStatus;
import com.ev.repository.ChargingSessionRepository;
import com.ev.repository.ReservationRepository;
import com.ev.service.IChargerService;
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
    private final IChargerService chargerService; //Cihaz durumunu güncellemek için

    @Override
    @Transactional
    public ChargingSessionDto startSession(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı!"));

        if (!ReservationStatus.CONFIRMED.equals(reservation.getStatus())) {
            throw new RuntimeException("Hata: Ödemesi yapılmamış veya geçersiz rezervasyonla seans başlatılamaz!");
        }

        // Zaman kontrolü
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resStart = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
        LocalDateTime resEnd = LocalDateTime.of(reservation.getReservationDate(), reservation.getEndTime());

        // Rezervasyondan çok önce başlanamaz
        if (now.isBefore(resStart.minusMinutes(15))) {
            throw new RuntimeException("Hata: Seans henüz başlatılamaz. Rezervasyon saatinize " +
                    java.time.Duration.between(now, resStart).toMinutes() + " dakika var.");
        }
        // Rezervasyon süresi geçmişse başlatılamaz
        if (now.isAfter(resEnd)) {
            throw new RuntimeException("Hata: Rezervasyon süreniz dolmuş. Yeni bir rezervasyon yapmalısınız.");
        }

        //Seans başlatma
        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(SessionStatus.ACTIVE);

        chargerService.updateStatus(reservation.getCharger().getId(), com.ev.model.enums.ChargerStatus.OCCUPIED);

        ChargingSession saved = chargingSessionRepository.save(session);

        ChargingSessionDto dto = new ChargingSessionDto();
        dto.setId(saved.getId());
        dto.setStartTime(saved.getStartTime());
        dto.setStatus(saved.getStatus());
        return dto;
    }

    @Override
    @Transactional
    public ChargingSessionDto endSession(Long sessionId, double energyConsumedKWh) {
        ChargingSession session = chargingSessionRepository
                .findById(sessionId).orElseThrow(() -> new RuntimeException("Seans bulunamadı!"));

        if (SessionStatus.FINISHED.equals(session.getStatus())) {
            throw new RuntimeException("Hata: Bu seans zaten sonlandırılmış!");
        }

        session.setEndTime(LocalDateTime.now());
        session.setEnergyConsumedKwh(energyConsumedKWh);
        session.setStatus(SessionStatus.FINISHED);

        //Gerçek maliyeti Hesaplama
        BigDecimal unitPrice = session.getReservation().getCharger().getStation().getPricingPerKWh();
        BigDecimal actualCost = unitPrice.multiply(BigDecimal.valueOf(energyConsumedKWh));
        session.setTotalCost(actualCost);

        long minutes = java.time.Duration.between(session.getReservation().getStartTime(),
                session.getReservation().getEndTime()).toMinutes();
        double hours = minutes / 60.0;
        double powerKw = session.getReservation().getCharger().getPowerOutput();

        BigDecimal prepaidAmount = unitPrice.multiply(BigDecimal.valueOf(powerKw * hours));

        // Eğer gerçek harcama peşin ödenenden azsa, farkı sürücüye iade et
        if (prepaidAmount.compareTo(actualCost) > 0) {
            BigDecimal refundAmount = prepaidAmount.subtract(actualCost);
            evDriverService.addBalance(session.getReservation().getDriver().getId(), refundAmount);
        }

        chargingSessionRepository.save(session);

        session.getReservation().setStatus(ReservationStatus.COMPLETED);
        chargerService.updateStatus(session.getReservation().getCharger().getId(), com.ev.model.enums.ChargerStatus.AVAILABLE);

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
