package com.ev.service;

import com.ev.dto.ChargerDto;
import com.ev.model.enums.ChargerStatus;

import java.util.List;

public interface IChargerService {
    ChargerDto save(ChargerDto chargerDto);
    List<ChargerDto> findByStationId(Long stationId);
    void updateStatus(Long chargerId, ChargerStatus newStatus);
}
