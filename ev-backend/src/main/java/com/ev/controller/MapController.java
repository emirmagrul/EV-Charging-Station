package com.ev.controller;

import com.ev.dto.StationMapDto;
import com.ev.service.IMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final IMapService mapService;

     //Harita görünümü için özel veri seti döner.
     //İçerik: Mesafe hesaplama, Renk Kodları (Durum) ve Filtreleme.
    @GetMapping("/stations")
    public ResponseEntity<List<StationMapDto>> getMapStations(
            @RequestParam double userLat,
            @RequestParam double userLng,
            @RequestParam(required = false) String connectorType) {
        return ResponseEntity.ok(mapService.getStationsForMap(userLat, userLng, connectorType));
    }

     //Seçilen istasyon için Google Maps navigasyon URL'i oluşturur.
    @GetMapping("/navigation/{stationId}")
    public ResponseEntity<String> getNavigationUrl(
            @PathVariable Long stationId,
            @RequestParam double userLat,
            @RequestParam double userLng) {
        return ResponseEntity.ok(mapService.getNavigationUrl(userLat, userLng, stationId));
    }
}