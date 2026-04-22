package com.ev.service;

import com.ev.dto.ChargerDto;

import java.util.List;

public interface IChargerService {
    ChargerDto save(ChargerDto chargerDto);
    List<ChargerDto> findByStationId(Long stationId);
    void updateStatus(Long chargerId, String newStatus); // R6 için önemli
}
