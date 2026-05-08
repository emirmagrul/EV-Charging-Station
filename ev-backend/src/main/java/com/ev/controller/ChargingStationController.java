package com.ev.controller;

import com.ev.dto.ChargingStationDto;
import com.ev.service.IChargingStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class ChargingStationController {

    private final IChargingStationService stationService;

    //Tüm istasyonları liste halinde getirir (İstasyon Kataloğu)
    @GetMapping
    public ResponseEntity<List<ChargingStationDto>> getAllStations() {
        return ResponseEntity.ok(stationService.findAll());
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<ChargingStationDto>> getByOperator(@PathVariable Long operatorId) {
        return ResponseEntity.ok(stationService.findByOperatorId(operatorId));
    }

    //Belirli bir istasyonun detaylarını getirir
    @GetMapping("/{id}")
    public ResponseEntity<ChargingStationDto> getStationById(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.findById(id));
    }
}