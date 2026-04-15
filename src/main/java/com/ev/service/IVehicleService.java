package com.ev.service;

import com.ev.dto.VehicleDto;

import java.util.List;

public interface IVehicleService {
    VehicleDto registerVehicle(VehicleDto vehicleDto);
    List<VehicleDto> findByDriverId(Long driverId);
}
