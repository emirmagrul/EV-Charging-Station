package com.ev.service;

import com.ev.dto.ChargingStationDto;

import java.util.List;

public interface IChargingStationService {
    ChargingStationDto save(ChargingStationDto stationDto, Long operatorId);
    List<ChargingStationDto> findAll();
    ChargingStationDto findById(Long id);
}
