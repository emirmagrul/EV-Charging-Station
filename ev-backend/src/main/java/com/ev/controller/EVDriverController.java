package com.ev.controller;

import com.ev.dto.EVDriverDto;
import com.ev.service.IEVDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class EVDriverController {

    private final IEVDriverService evDriverService;

    @PostMapping("/register")
    public ResponseEntity<EVDriverDto> register(@RequestBody EVDriverDto driverDto) {
        return ResponseEntity.ok(evDriverService.createDriver(driverDto));
    }

    @PostMapping("/login")
    public ResponseEntity<EVDriverDto> login(@RequestBody EVDriverDto loginDto) {
        return ResponseEntity.ok(evDriverService.login(loginDto.getEmail(), loginDto.getPassword()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<EVDriverDto> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(evDriverService.findById(id));
    }

    @PostMapping("/{id}/balance")
    public ResponseEntity<String> addBalance(@PathVariable Long id, @RequestParam BigDecimal amount) {
        evDriverService.addBalance(id, amount);
        return ResponseEntity.ok("Bakiye başarıyla eklendi.");
    }

    @PostMapping("/{id}/favorites/{stationId}")
    public ResponseEntity<String> addFavorite(@PathVariable Long id, @PathVariable Long stationId) {
        evDriverService.addStationToFavorites(id, stationId);
        return ResponseEntity.ok("İstasyon favorilere eklendi.");
    }

    @DeleteMapping("/{id}/favorites/{stationId}")
    public ResponseEntity<String> removeFavorite(@PathVariable Long id, @PathVariable Long stationId) {
        evDriverService.removeStationFromFavorites(id, stationId);
        return ResponseEntity.ok("İstasyon favorilerden çıkarıldı.");
    }

    @GetMapping("/{id}/favorites")
    public ResponseEntity<java.util.List<com.ev.dto.ChargingStationDto>> getFavorites(@PathVariable Long id) {
        return ResponseEntity.ok(evDriverService.getFavoriteStations(id));
    }
}