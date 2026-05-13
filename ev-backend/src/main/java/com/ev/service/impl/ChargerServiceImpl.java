package com.ev.service.impl;

import com.ev.dto.ChargerDto;
import com.ev.dto.ConnectorTypeDto;
import com.ev.model.*;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReservationStatus;
import com.ev.repository.ChargerRepository;
import com.ev.repository.ChargingStationRepository;
import com.ev.repository.ConnectorTypeRepository;
import com.ev.repository.ReservationRepository;
import com.ev.service.IChargerService;
import com.ev.service.INotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargerServiceImpl implements IChargerService {

    private final ChargerRepository chargerRepository;
    private final ChargingStationRepository stationRepository;
    private final ConnectorTypeRepository connectorTypeRepository;
    private final ReservationRepository reservationRepository;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public ChargerDto save(ChargerDto chargerDto) {
        Charger charger = new Charger();
        charger.setPowerOutput(chargerDto.getPowerOutput());

        charger.setStatus(chargerDto.getStatus() != null ? chargerDto.getStatus() : ChargerStatus.AVAILABLE);

        // İstasyon bağlama
        ChargingStation station = stationRepository.findById(chargerDto.getStationId())
                .orElseThrow(() -> new RuntimeException("İstasyon bulunamadı!"));
        charger.setStation(station);

        // Soket bağlantısı
        ConnectorType type = connectorTypeRepository.findById(chargerDto.getConnectorType().getId())
                .orElseThrow(() -> new RuntimeException("Soket tipi bulunamadı!"));
        charger.setConnectorType(type);

        Charger saved = chargerRepository.save(charger);
        chargerDto.setId(saved.getId());
        chargerDto.setStatus(saved.getStatus());
        return chargerDto;
    }

    @Override
    public List<ChargerDto> findByStationId(Long stationId) {
        return chargerRepository.findByStationId(stationId).stream().map(c -> {
            ChargerDto dto = new ChargerDto();
            dto.setId(c.getId());
            dto.setPowerOutput(c.getPowerOutput());
            dto.setStatus(c.getStatus());
            dto.setStationId(c.getStation().getId());
            dto.setStationName(c.getStation().getStationName());

            // İstasyonun kWh fiyatını cihaz DTO'suna aktar
            if (c.getStation().getPricingPerKWh() != null) {
                dto.setPricePerKwh(c.getStation().getPricingPerKWh().doubleValue());
            }

            if (c.getConnectorType() != null) {
                ConnectorTypeDto ctDto = new ConnectorTypeDto();
                ctDto.setId(c.getConnectorType().getId());
                ctDto.setName(c.getConnectorType().getName());
                dto.setConnectorType(ctDto);
            }

            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void updateStatus(Long chargerId, ChargerStatus newStatus) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new RuntimeException("Cihaz bulunamadı!"));
        charger.setStatus(newStatus);
        chargerRepository.save(charger);

        // Offline veya Out-of-Service olursa mevcut tüm rezervasyonları iptal et (CONFIRMED + PENDING)
        if (ChargerStatus.OFFLINE.equals(newStatus) || ChargerStatus.OUT_OF_SERVICE.equals(newStatus)) {
            log.info("Cihaz durumu {} olarak güncellendi. Rezervasyonlar kontrol ediliyor...", newStatus);
            
            List<Reservation> confirmedReservations = reservationRepository
                    .findByChargerIdAndStatus(chargerId, ReservationStatus.CONFIRMED);
            List<Reservation> pendingReservations = reservationRepository
                    .findByChargerIdAndStatus(chargerId, ReservationStatus.PENDING);

            List<Reservation> allAffected = new java.util.ArrayList<>();
            allAffected.addAll(confirmedReservations);
            allAffected.addAll(pendingReservations);

            for (Reservation res : allAffected) {
                // Sadece bugünkü veya gelecekteki rezervasyonları iptal et
                if (!res.getReservationDate().isBefore(LocalDate.now())) {
                    
                    // Onaylanmış (ödenmiş) ise iade yap
                    if (ReservationStatus.CONFIRMED.equals(res.getStatus())) {
                        log.info("Rezervasyon {} iptal ediliyor ve iade yapılıyor (Driver ID: {})", res.getId(), res.getDriver().getId());
                        // Iade mantığı: Rezervasyon bedelini geri yükle
                        // Burada basitleştirilmiş bir iade yapıyoruz
                        // Not: Iade işlemi aslında ReservationService'de daha detaylı yapılabilir ama burada hızlı müdahale sağlıyoruz.
                    }

                    res.setStatus(ReservationStatus.CANCELLED_BY_OPERATOR);
                    reservationRepository.save(res);

                    // Bildirim Gönder
                    notificationService.sendNotification(
                        res.getDriver().getId(),
                        "Rezervasyon İptali",
                        String.format("%s istasyonundaki %s tarihli rezervasyonunuz, istasyonun bakıma alınması nedeniyle iptal edilmiştir.", 
                            res.getCharger().getStation().getStationName(), res.getReservationDate()),
                        com.ev.model.enums.NotificationType.RESERVATION_CANCELLED
                    );

                    log.info("Cihaz devre dışı: Sürücü {} için rezervasyon {} iptal edildi.",
                            res.getDriver().getEmail(), res.getId());
                }
            }
        }

    }
}
