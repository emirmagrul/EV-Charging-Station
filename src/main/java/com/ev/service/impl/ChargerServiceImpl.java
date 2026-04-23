package com.ev.service.impl;

import com.ev.dto.ChargerDto;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

        //İstasyon bağlama
        ChargingStation station = stationRepository.findById(chargerDto.getStationId())
                .orElseThrow(() -> new RuntimeException("İstasyon bulunamadı!"));
        charger.setStation(station);

        //Soket bağlantısı
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
                dto.getConnectorType().setId(c.getConnectorType().getId());
                dto.getConnectorType().setName(c.getConnectorType().getName());
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

        //Offline olursa mevcut tüm rezervasyonları iptal et
        if (ChargerStatus.OFFLINE.equals(newStatus)) {
            List<Reservation> affectedReservations = reservationRepository
                    .findByChargerIdAndStatus(chargerId, ReservationStatus.CONFIRMED);

            for (Reservation res : affectedReservations) {
                if (!res.getReservationDate().isBefore(LocalDate.now())) {
                    res.setStatus(ReservationStatus.CANCELLED_BY_OPERATOR);
                    reservationRepository.save(res);

                    System.out.println("BİLDİRİM: Sürücü " + res.getDriver().getEmail() +
                            " için rezervasyon iptal edildi. Sebep: Cihaz Bakımı.");
                }
            }
        }

    }
}
