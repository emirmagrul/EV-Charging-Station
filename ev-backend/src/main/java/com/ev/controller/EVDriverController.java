package com.ev.controller;

import com.ev.dto.EVDriverDto;
import com.ev.dto.StationOperatorDto;
import com.ev.service.IAdminService;
import com.ev.service.IEVDriverService;
import com.ev.service.IStationOperatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Slf4j
public class EVDriverController {

    private final IEVDriverService evDriverService;
    private final IStationOperatorService operatorService;
    private final IAdminService adminService;

    @PostMapping("/register")
    public ResponseEntity<EVDriverDto> register(@RequestBody EVDriverDto driverDto) {
        log.info("Kayıt isteği alındı: Email={}, Rol={}", driverDto.getEmail(), driverDto.getRole());
        
        if (com.ev.model.enums.UserRole.OPERATOR.equals(driverDto.getRole())) {
            log.info("Operatör kaydı işleniyor...");
            StationOperatorDto operatorDto = new StationOperatorDto();
            operatorDto.setFirstName(driverDto.getFirstName());
            operatorDto.setLastName(driverDto.getLastName());
            operatorDto.setEmail(driverDto.getEmail());
            operatorDto.setPassword(driverDto.getPassword());
            operatorDto.setRole(com.ev.model.enums.UserRole.OPERATOR);
            
            StationOperatorDto saved = operatorService.save(operatorDto);
            log.info("Operatör başarıyla kaydedildi: ID={}", saved.getId());
            
            EVDriverDto response = new EVDriverDto();
            response.setId(saved.getId());
            response.setFirstName(saved.getFirstName());
            response.setLastName(saved.getLastName());
            response.setEmail(saved.getEmail());
            response.setRole(com.ev.model.enums.UserRole.OPERATOR);
            return ResponseEntity.ok(response);
        }
        
        if (com.ev.model.enums.UserRole.ADMIN.equals(driverDto.getRole())) {
            log.info("Admin kaydı işleniyor...");
            com.ev.model.Admin admin = new com.ev.model.Admin();
            admin.setFirstName(driverDto.getFirstName());
            admin.setLastName(driverDto.getLastName());
            admin.setEmail(driverDto.getEmail());
            admin.setPassword(driverDto.getPassword());
            admin.setRole(com.ev.model.enums.UserRole.ADMIN);

            com.ev.model.Admin saved = adminService.save(admin);
            log.info("Admin başarıyla kaydedildi: ID={}", saved.getId());

            EVDriverDto response = new EVDriverDto();
            response.setId(saved.getId());
            response.setFirstName(saved.getFirstName());
            response.setLastName(saved.getLastName());
            response.setEmail(saved.getEmail());
            response.setRole(com.ev.model.enums.UserRole.ADMIN);
            return ResponseEntity.ok(response);
        }

        log.info("Sürücü kaydı işleniyor...");
        return ResponseEntity.ok(evDriverService.createDriver(driverDto));
    }

    @PostMapping("/login")
    public ResponseEntity<EVDriverDto> login(@RequestBody EVDriverDto loginDto) {
        // loginDto.getRole() artık UserRole tipinde dönecek
        return ResponseEntity.ok(evDriverService.login(loginDto.getEmail(), loginDto.getPassword(), loginDto.getRole()));
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