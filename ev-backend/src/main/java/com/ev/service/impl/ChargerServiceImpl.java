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

        // Offline olursa mevcut tüm rezervasyonları iptal et (CONFIRMED + PENDING)
        if (ChargerStatus.OFFLINE.equals(newStatus)) {
            List<Reservation> confirmedReservations = reservationRepository
                    .findByChargerIdAndStatus(chargerId, ReservationStatus.CONFIRMED);
            List<Reservation> pendingReservations = reservationRepository
                    .findByChargerIdAndStatus(chargerId, ReservationStatus.PENDING);

            List<Reservation> allAffected = new java.util.ArrayList<>();
            allAffected.addAll(confirmedReservations);
            allAffected.addAll(pendingReservations);

            for (Reservation res : allAffected) {
                if (!res.getReservationDate().isBefore(LocalDate.now())) {
                    res.setStatus(ReservationStatus.CANCELLED_BY_OPERATOR);
                    reservationRepository.save(res);
                    log.info("Cihaz bakıma alındı: Sürücü {} için rezervasyon {} iptal edildi.",
                            res.getDriver().getEmail(), res.getId());
                }
            }
        }

    }
}
