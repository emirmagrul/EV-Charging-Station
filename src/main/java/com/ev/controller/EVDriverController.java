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

    @GetMapping("/{id}")
    public ResponseEntity<EVDriverDto> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(evDriverService.findById(id));
    }

    @PostMapping("/{id}/balance")
    public ResponseEntity<String> addBalance(@PathVariable Long id, @RequestParam BigDecimal amount) {
        evDriverService.addBalance(id, amount);
        return ResponseEntity.ok("Bakiye başarıyla eklendi.");
    }
}