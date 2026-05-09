package com.ev.controller;

import com.ev.dto.ChargingStationDto;
import com.ev.dto.RevenueReportDto;
import com.ev.dto.SystemHealthDto;
import com.ev.dto.UserActivityDto;
import com.ev.service.IAdminService;
import com.ev.service.IChargingStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;
    // İstasyon fiyatları/saatlerini güncellemek için ekliyoruz
    private final IChargingStationService stationService;

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

    // --- 3. SİSTEM SAĞLIĞI VE DENETİM ---

    @GetMapping("/health")
    public ResponseEntity<SystemHealthDto> getSystemHealth() {
        return ResponseEntity.ok(adminService.getSystemHealthStatus());
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