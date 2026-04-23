package com.ev.service.impl;

import com.ev.dto.ReservationDto;
import com.ev.model.Charger;
import com.ev.model.EVDriver;
import com.ev.model.Reservation;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReservationStatus;
import com.ev.repository.ChargerRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.ReservationRepository;
import com.ev.repository.VehicleRepository;
import com.ev.service.IEVDriverService;
import com.ev.service.IReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final EVDriverRepository evDriverRepository;
    private final ChargerRepository chargerRepository;
    private final VehicleRepository vehicleRepository;
    private final IEVDriverService evDriverService;

    @Override
    @Transactional
    public ReservationDto makeReservation(ReservationDto reservationDto) {

        //Rezervasyon süresi en fazla 2 saat olabilir.
        long durationMinutes = Duration.between(reservationDto.getStartTime(), reservationDto.getEndTime()).toMinutes();
        if (durationMinutes <= 0 || durationMinutes > 120) {
            throw new RuntimeException("Hata: Rezervasyon süresi 2 saati aşamaz!");
        }

        //En geç 24 saat önceden rezervasyon yapılabilir.
        LocalDateTime requestedStart = LocalDateTime.of(reservationDto.getReservationDate(), reservationDto.getStartTime());
        if (requestedStart.isAfter(LocalDateTime.now().plusHours(24))) {
            throw new RuntimeException("Hata: Rezervasyonlar en fazla 24 saat öncesinden yapılabilir!");
        }
        if (requestedStart.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Hata: Geçmiş bir zamana rezervasyon yapılamaz!");
        }

        Charger charger = chargerRepository.findById(reservationDto.getChargerId())
                .orElseThrow(() -> new RuntimeException("Şarj ünitesi bulunamadı"));

        //Operator Bakım Kontrolü
        if (ChargerStatus.OFFLINE.equals(charger.getStatus())) {
            throw new RuntimeException("Hata: Bu şarj ünitesi şu an bakımda (OFFLINE) olduğu için rezervasyon yapılamaz!");
        }

        EVDriver driver = evDriverRepository.findById(reservationDto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı"));

        //Araç-Şarj Ünitesi Uyumluluk Kontrolü
        //Aracın ünitedeki şarj soket tipini desteklemesi gerekiyor
        boolean isCompatible = vehicleRepository.findByOwnerId(driver.getId()).stream()
                .anyMatch(v -> v.getConnectorType().getId().equals(charger.getConnectorType().getId()));

        if (!isCompatible) {
            throw new RuntimeException("Hata: Aracınız bu soket tipiyle uyumlu değil!");
        }

        //Aynı gün ve aynı şarj ünitesi için çakışan CONFIRMED rezervasyon var mı?
        boolean isOverlapping = reservationRepository.existsOverlappingReservation(
                charger.getId(),
                reservationDto.getReservationDate(),
                reservationDto.getStartTime(),
                reservationDto.getEndTime(),
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING)
        );

        if (isOverlapping) {
            throw new RuntimeException("Hata: Seçilen zaman dilimi dolu!");
        }

        Reservation reservation = new Reservation();
        reservation.setReservationDate(reservationDto.getReservationDate());
        reservation.setStartTime(reservationDto.getStartTime());
        reservation.setEndTime(reservationDto.getEndTime());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setDriver(driver);
        reservation.setCharger(charger);

        Reservation saved = reservationRepository.save(reservation);
        reservationDto.setId(saved.getId());
        reservationDto.setStationName(charger.getStation().getStationName());
        reservationDto.setStatus(saved.getStatus());
        return reservationDto;
    }

    @Override
    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        if (!ReservationStatus.PENDING.equals(res.getStatus())) {
            throw new RuntimeException("Hata: Rezervasyon bekleyen (PENDING) durumunda değil!");
        }

        long minutes = Duration.between(res.getStartTime(), res.getEndTime()).toMinutes();
        double hours = minutes / 60.0;
        BigDecimal unitPrice = res.getCharger().getStation().getPricingPerKWh();
        double powerKw = res.getCharger().getPowerOutput();

        BigDecimal estimatedCost = unitPrice.multiply(BigDecimal.valueOf(powerKw * hours));

        evDriverService.deductBalance(res.getDriver().getId(), estimatedCost);

        res.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(res);

        System.out.println("ÖDEME ONAYI: Sürücüden " + estimatedCost + " TL tahsil edildi.");
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        if (!ReservationStatus.CONFIRMED.equals(res.getStatus())) {
            throw new RuntimeException("Hata: Sadece onaylanmış (CONFIRMED) rezervasyonlar iptal edilebilir.");
        }

        LocalDateTime reservationStart = LocalDateTime.of(res.getReservationDate(), res.getStartTime());
        if (LocalDateTime.now().isAfter(reservationStart)) {
            throw new RuntimeException("Hata: Başlama saati geçmiş olan bir rezervasyon iptal edilemez!");
        }

        BigDecimal unitPrice = res.getCharger().getStation().getPricingPerKWh();
        double powerKw = res.getCharger().getPowerOutput();
        long minutes = Duration.between(res.getStartTime(), res.getEndTime()).toMinutes();
        BigDecimal prepaidAmount = unitPrice.multiply(BigDecimal.valueOf(powerKw * (minutes / 60.0)));

        evDriverService.addBalance(res.getDriver().getId(), prepaidAmount);

        res.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(res);
    }

    @Override
    public List<ReservationDto> getMyReservations(Long driverId) {
        return reservationRepository.findByDriverId(driverId).stream().map(res -> {
            ReservationDto dto = new ReservationDto();
            dto.setId(res.getId());
            dto.setReservationDate(res.getReservationDate());
            dto.setStartTime(res.getStartTime());
            dto.setEndTime(res.getEndTime());
            dto.setStatus(res.getStatus());
            dto.setDriverId(res.getDriver().getId());
            dto.setChargerId(res.getCharger().getId());

            // Reservation -> Charger -> ChargingStation -> stationName
            if (res.getCharger() != null && res.getCharger().getStation() != null) {
                dto.setStationName(res.getCharger().getStation().getStationName());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
