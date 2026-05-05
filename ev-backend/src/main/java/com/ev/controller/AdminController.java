package com.ev.controller;

import com.ev.service.IAdminService;
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

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(adminService.getTotalRevenue());
    }

    @GetMapping("/stats/stations")
    public ResponseEntity<Map<String, Long>> getStationStats() {
        return ResponseEntity.ok(adminService.getStationUsageStats());
    }

    @GetMapping("/stats/peak-hours")
    public ResponseEntity<Map<Integer, Long>> getPeakHours() {
        return ResponseEntity.ok(adminService.getPeakHours());
    }
}