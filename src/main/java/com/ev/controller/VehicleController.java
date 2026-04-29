package com.ev.controller;

import com.ev.dto.VehicleDto;
import com.ev.service.IVehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final IVehicleService vehicleService;

    @PostMapping("/register")
    public ResponseEntity<VehicleDto> registerVehicle(@RequestBody VehicleDto vehicleDto) {
        return ResponseEntity.ok(vehicleService.registerVehicle(vehicleDto));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehicleDto>> getDriverVehicles(@PathVariable Long driverId) {
        return ResponseEntity.ok(vehicleService.findByDriverId(driverId));
    }
}
