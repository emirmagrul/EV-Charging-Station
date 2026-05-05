package com.ev.controller;

import com.ev.dto.ChargerDto;
import com.ev.model.enums.ChargerStatus;
import com.ev.service.IChargerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chargers")
@RequiredArgsConstructor
public class ChargerController {

    private final IChargerService chargerService;

    //Yeni bir şarj ünitesi ekler
    @PostMapping
    public ResponseEntity<ChargerDto> save(@RequestBody ChargerDto chargerDto) {
        return ResponseEntity.ok(chargerService.save(chargerDto));
    }

    //Belirli bir istasyona ait tüm üniteleri getirir
    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<ChargerDto>> getByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(chargerService.findByStationId(stationId));
    }

    //Cihazın durumunu günceller (Müsait, Dolu veya Bakımda/Offline)
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam ChargerStatus status) {
        chargerService.updateStatus(id, status);
        return ResponseEntity.ok("Cihaz durumu güncellendi.");
    }
}