package com.ev.controller;

import com.ev.dto.*;
import com.ev.service.IAdminService;
import com.ev.service.IChargingStationService;
import com.ev.service.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;
    private final IChargingStationService stationService;
    private final IReservationService reservationService;

    // --- 1. İDARİ RAPORLAMA VE ANALİZ ---

    @GetMapping("/reports/revenue")
    public ResponseEntity<RevenueReportDto> getRevenueReport() {
        return ResponseEntity.ok(adminService.getRevenueReport());
    }

    @GetMapping("/reports/user-activity")
    public ResponseEntity<UserActivityDto> getUserActivitySummary() {
        return ResponseEntity.ok(adminService.getUserActivitySummary());
    }

    // --- 2. AĞ PERFORMANSI VE OPTİMİZASYONU ---

    @GetMapping("/performance/peak-hours")
    public ResponseEntity<Map<Integer, Long>> getPeakHours() {
        return ResponseEntity.ok(adminService.getPeakHourAnalysis());
    }

    @GetMapping("/performance/peak-hours/{stationId}")
    public ResponseEntity<Map<Integer, Long>> getPeakHoursByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(adminService.getPeakHourAnalysisByStation(stationId));
    }

    // --- 3. SİSTEM SAĞLIĞI VE DENETİM ---

    @GetMapping("/health")
    public ResponseEntity<SystemHealthDto> getSystemHealth() {
        return ResponseEntity.ok(adminService.getSystemHealthStatus());
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    // --- 4. YÜKSEK DÜZEY YAPILANDIRMA (KURAL YÖNETİMİ) ---

    // Admin'in bir istasyonun fiyatını veya çalışma saatlerini güncellemesi
    @PatchMapping("/config/station/{stationId}")
    public ResponseEntity<ChargingStationDto> updateStationConfig(
            @PathVariable Long stationId,
            @RequestParam(required = false) BigDecimal pricingPerKWh,
            @RequestParam(required = false) String operatingHours) {

        ChargingStationDto updatedStation = stationService.updateStationConfig(stationId, pricingPerKWh, operatingHours);
        return ResponseEntity.ok(updatedStation);
    }
}