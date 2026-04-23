package com.ev.service;

import com.ev.dto.ChargingStationDto;
import com.ev.dto.EVDriverDto;
import com.ev.model.EVDriver;

import java.math.BigDecimal;
import java.util.List;

public interface IEVDriverService {
    EVDriverDto createDriver(EVDriverDto evDriverDto);
    void addBalance(Long driverId, BigDecimal amount);
    void deductBalance(Long driverId, BigDecimal amount);
    EVDriverDto findById(Long id);
    void addStationToFavorites(Long driverId, Long stationId);
    void removeStationFromFavorites(Long driverId, Long stationId);
    List<ChargingStationDto> getFavoriteStations(Long driverId);
}
