package com.ev.service.impl;

import com.ev.dto.VehicleDto;
import com.ev.model.ConnecterType;
import com.ev.model.EVDriver;
import com.ev.model.Vehicle;
import com.ev.repository.ConnecterTypeRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.VehicleRepository;
import com.ev.service.IVehicleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements IVehicleService {

    private final VehicleRepository vehicleRepository;
    private final EVDriverRepository evDriverRepository;
    private final ConnecterTypeRepository connecterTypeRepository;


    @Override
    @Transactional
    public VehicleDto registerVehicle(VehicleDto vehicleDto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(vehicleDto.getBrand());
        vehicle.setModel(vehicleDto.getModel());
        vehicle.setBatteryCapacity(vehicleDto.getBatteryCapacity());
        vehicle.setPlateNumber(vehicleDto.getPlateNumber());

        EVDriver owner = evDriverRepository.findById(vehicleDto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Sahip Sürücü bulunamadı!"));

        ConnecterType type = connecterTypeRepository.findById(vehicleDto.getConnectorType().getId())
                .orElseThrow(() -> new RuntimeException("Soket tipi bulunamadı!"));

        vehicle.setOwner(owner);
        vehicle.setConnectorType(type);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        vehicleDto.setId(savedVehicle.getId());
        return vehicleDto;
    }

    @Override
    @Transactional
    public List<VehicleDto> findByDriverId(Long driverId) {
        return vehicleRepository.findByOwnerId(driverId).stream().map(v -> {
            VehicleDto dto = new VehicleDto();
            dto.setId(v.getId());
            dto.setBrand(v.getBrand());
            dto.setModel(v.getModel());
            dto.setPlateNumber(v.getPlateNumber());
            dto.setBatteryCapacity(v.getBatteryCapacity());
            dto.setDriverId(v.getOwner().getId());
            return dto;
        }).collect(Collectors.toList());
    }
}
