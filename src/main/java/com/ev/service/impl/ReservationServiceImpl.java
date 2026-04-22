package com.ev.service.impl;

import com.ev.dto.ReservationDto;
import com.ev.model.Charger;
import com.ev.model.EVDriver;
import com.ev.model.Reservation;
import com.ev.repository.ChargerRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.ReservationRepository;
import com.ev.repository.VehicleRepository;
import com.ev.service.IReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    @Override
    @Transactional
    public ReservationDto makeReservation(ReservationDto reservationDto) {

        //Rezervasyon süresi en fazla 2 saat olabilir.
        long durationMinutes = Duration.between(reservationDto.getStartTime(), reservationDto.getEndTime()).toMinutes();
        if (durationMinutes <= 0 || durationMinutes > 120) {
            throw new RuntimeException("Hata: Rezervasyon süresi 2 saati aşamaz ve başlangıç zamanı bitişten önce olmalıdır!");
        }

        //En geç 24 saat önceden rezervasyon yapılabilir.
        LocalDateTime requestedStart = LocalDateTime.of(reservationDto.getReservationDate(), reservationDto.getStartTime());
        if (requestedStart.isAfter(LocalDateTime.now().plusHours(24))) {
            throw new RuntimeException("Hata: Rezervasyonlar en fazla 24 saat öncesinden yapılabilir!");
        }
        if (requestedStart.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Hata: Geçmiş bir zamana rezervasyon yapılamaz!");
        }

        EVDriver driver = evDriverRepository.findById(reservationDto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı"));

        Charger charger = chargerRepository.findById(reservationDto.getChargerId())
                .orElseThrow(() -> new RuntimeException("Şarj ünitesi bulunamadı"));

        //Operator Bakım Kontrolü
        if ("OFFLINE".equalsIgnoreCase(charger.getStatus())) {
            throw new RuntimeException("Hata: Bu şarj ünitesi şu an bakımda (OFFLINE) olduğu için rezervasyon yapılamaz!");
        }

        //Araç-Şarj Ünitesi Uyumluluk Kontrolü
        //Aracın ünitedeki şarj soket tipini desteklemesi gerekiyor
        boolean isCompatible = vehicleRepository.findByOwnerId(driver.getId()).stream()
                .anyMatch(v -> v.getConnectorType().getId().equals(charger.getConnectorType().getId()));

        if (!isCompatible) {
            throw new RuntimeException("Hata: Aracınızın soket tipi bu şarj ünitesi ile uyumlu değil!");
        }

        //Aynı gün ve aynı şarj ünitesi için çakışan CONFIRMED rezervasyon var mı?
        boolean isOverlapping = reservationRepository.findAll().stream()
                .filter(r -> r.getCharger().getId().equals(charger.getId()))
                .filter(r -> r.getReservationDate().equals(reservationDto.getReservationDate()))
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .anyMatch(r -> reservationDto.getStartTime().isBefore(r.getEndTime()) &&
                        reservationDto.getEndTime().isAfter(r.getStartTime()));

        if (isOverlapping) {
            throw new RuntimeException("Hata: Seçilen zaman dilimi başka bir kullanıcı tarafından rezerve edilmiş!");
        }


        Reservation reservation = new Reservation();
        reservation.setReservationDate(reservationDto.getReservationDate());
        reservation.setStartTime(reservationDto.getStartTime());
        reservation.setEndTime(reservationDto.getEndTime());
        reservation.setStatus("CONFIRMED");
        reservation.setDriver(driver);
        reservation.setCharger(charger);

        Reservation saved = reservationRepository.save(reservation);
        reservationDto.setId(saved.getId());
        reservationDto.setStationName(charger.getStation().getStationName());
        return reservationDto;
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));
        res.setStatus("CANCELLED");
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
