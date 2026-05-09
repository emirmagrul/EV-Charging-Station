package com.ev.service.impl;

import com.ev.dto.ChargingStationDto;
import com.ev.model.ChargingStation;
import com.ev.model.StationOperator;
import com.ev.model.enums.ChargerStatus;
import com.ev.repository.ChargingStationRepository;
import com.ev.repository.StationOperatorRepository;
import com.ev.service.IChargingStationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargingStationServiceImpl implements IChargingStationService {

    private final ChargingStationRepository stationRepository;
    private final StationOperatorRepository operatorRepository;

    @Override
    @Transactional
    public ChargingStationDto save(ChargingStationDto stationDto, Long operatorId) {
        ChargingStation station = new ChargingStation();
        station.setStationName(stationDto.getStationName());
        station.setAddress(stationDto.getAddress());
        station.setLatitude(stationDto.getLatitude());
        station.setLongitude(stationDto.getLongitude());
        station.setOperatingHours(stationDto.getOperatingHours());
        station.setPricingPerKWh(stationDto.getPricingPerKWh());

        //İstasyonu operatöre bağlama
        StationOperator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("İstasyon için geçerli bir operatör bulunamadı!"));
        station.setResponsibleOperator(operator);

        ChargingStation saved = stationRepository.save(station);
        stationDto.setId(saved.getId());
        return stationDto;
    }

    @Override
    @Transactional
    public List<ChargingStationDto> findAll() {
        return stationRepository.findAll().stream().map(s -> {
            return getChargingStationDto(s);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ChargingStationDto> findByOperatorId(Long operatorId) {
        log.info("Operatör ID {} için istasyonlar aranıyor...", operatorId);
        List<ChargingStation> stations = stationRepository.findByResponsibleOperatorId(operatorId);
        log.info("Bulunan istasyon sayısı: {}", stations.size());

        stations.forEach(s -> log.info("Bulunan İstasyon: ID={}, Name={}", s.getId(), s.getStationName()));

        return stations.stream().map(s -> {
            return getChargingStationDto(s);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChargingStationDto findById(Long id) {
        ChargingStation s = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İstasyon bulunamadı!"));
        return getChargingStationDto(s);
    }

    // YENİ: Fiyat ve saat güncelleme metodu (Admin ve Operatör kullanımı için)
    @Override
    @Transactional
    public ChargingStationDto updateStationConfig(Long stationId, BigDecimal newPricing, String newOperatingHours) {
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Güncellenecek istasyon bulunamadı!"));

        if (newPricing != null && newPricing.compareTo(BigDecimal.ZERO) > 0) {
            station.setPricingPerKWh(newPricing);
            log.info("İstasyon {} (ID: {}) fiyatı {} olarak güncellendi.", station.getStationName(), station.getId(), newPricing);
        }

        if (newOperatingHours != null && !newOperatingHours.isEmpty()) {
            station.setOperatingHours(newOperatingHours);
            log.info("İstasyon {} (ID: {}) çalışma saatleri '{}' olarak güncellendi.", station.getStationName(), station.getId(), newOperatingHours);
        }

        ChargingStation updatedStation = stationRepository.save(station);
        return getChargingStationDto(updatedStation);
    }

    private ChargingStationDto getChargingStationDto(ChargingStation s) {
        ChargingStationDto dto = new ChargingStationDto();
        dto.setId(s.getId());
        dto.setStationName(s.getStationName());
        dto.setAddress(s.getAddress());
        dto.setLatitude(s.getLatitude());
        dto.setLongitude(s.getLongitude());
        dto.setOperatingHours(s.getOperatingHours());
        dto.setPricingPerKWh(s.getPricingPerKWh());

        if (s.getChargers() != null) {
            List<Long> connectorIds = s.getChargers().stream()
                    .filter(c -> c.getConnectorType() != null)
                    .map(c -> c.getConnectorType().getId())
                    .distinct()
                    .collect(Collectors.toList());
            dto.setSupportedConnectorTypeIds(connectorIds);

            // İstasyonun genel durumunu şarj ünitelerinden türet
            boolean hasAvailable = s.getChargers().stream()
                    .anyMatch(c -> ChargerStatus.AVAILABLE.equals(c.getStatus()));
            boolean allOffline = s.getChargers().stream()
                    .allMatch(c -> ChargerStatus.OFFLINE.equals(c.getStatus()));

            if (hasAvailable) {
                dto.setStatus(ChargerStatus.AVAILABLE);
            } else if (allOffline) {
                dto.setStatus(ChargerStatus.OFFLINE);
            } else {
                dto.setStatus(ChargerStatus.OCCUPIED);
            }
        } else {
            dto.setStatus(ChargerStatus.OFFLINE);
        }

        return dto;

    }
}