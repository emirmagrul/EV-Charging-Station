package com.ev.service.impl;

import com.ev.dto.ChargerDto;
import com.ev.model.Charger;
import com.ev.model.ChargingStation;
import com.ev.model.ConnecterType;
import com.ev.model.StationOperator;
import com.ev.repository.ChargerRepository;
import com.ev.repository.ChargingStationRepository;
import com.ev.repository.ConnecterTypeRepository;
import com.ev.service.IChargerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargerServiceImpl implements IChargerService {

    private final ChargerRepository chargerRepository;
    private final ChargingStationRepository stationRepository;
    private final ConnecterTypeRepository connecterTypeRepository;

    @Override
    @Transactional
    public ChargerDto save(ChargerDto chargerDto) {
        Charger charger = new Charger();
        charger.setPowerOutput(chargerDto.getPowerOutput());
        charger.setStatus(chargerDto.getStatus());

        //İstasyon bağlama
        ChargingStation station = stationRepository.findById(chargerDto.getStationId())
                .orElseThrow(() -> new RuntimeException("Soket tipi bulunamadı!"));
        charger.setStation(station);

        //Soket bağlantısı
        ConnecterType type = connecterTypeRepository.findById(chargerDto.getConnectorType().getId())
                .orElseThrow(() -> new RuntimeException("Soket tipi bulunamadı!"));
        charger.setConnectorType(type);

        Charger saved = chargerRepository.save(charger);
        chargerDto.setId(saved.getId());
        return chargerDto;
    }

    @Override
    public List<ChargerDto> findByStationId(Long stationId) {
        return chargerRepository.findByStationIdAndStatus(stationId, null).stream().map(c -> {
            ChargerDto dto = new ChargerDto();
            dto.setId(c.getId());
            dto.setPowerOutput(c.getPowerOutput());
            dto.setStatus(c.getStatus());
            dto.setStationId(c.getStation().getId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long chargerId, String newStatus) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new RuntimeException("Cihaz bulunamadı!"));
        charger.setStatus(newStatus);
        chargerRepository.save(charger);
    }
}
