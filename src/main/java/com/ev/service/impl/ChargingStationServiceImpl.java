package com.ev.service.impl;

import com.ev.dto.ChargingStationDto;
import com.ev.model.ChargingStation;
import com.ev.model.StationOperator;
import com.ev.repository.ChargingStationRepository;
import com.ev.repository.StationOperatorRepository;
import com.ev.service.IChargingStationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    public List<ChargingStationDto> findAll() {
        return stationRepository.findAll().stream().map(s -> {
            return getChargingStationDto(s);
        }).collect(Collectors.toList());
    }

    @Override
    public ChargingStationDto findById(Long id) {
        ChargingStation s = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İstasyon bulunamadı!"));
        return getChargingStationDto(s);
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
        return dto;
    }
}
