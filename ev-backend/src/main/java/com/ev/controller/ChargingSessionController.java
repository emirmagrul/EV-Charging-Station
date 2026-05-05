package com.ev.controller;

import com.ev.dto.ChargingSessionDto;
import com.ev.service.IChargingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class ChargingSessionController {

    private final IChargingSessionService chargingSessionService;

    //Şarj seansını başlatır (Cihaz durumunu OCCUPIED yapar)
    @PostMapping("/start/{reservationId}")
    public ResponseEntity<ChargingSessionDto> startSession(@PathVariable Long reservationId) {
        return ResponseEntity.ok(chargingSessionService.startSession(reservationId));
    }

    //Şarjı bitirir, tüketimi hesaplar ve gerekirse iade yapar
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ChargingSessionDto> endSession(
            @PathVariable Long sessionId,
            @RequestParam double energyConsumedKWh) {
        return ResponseEntity.ok(chargingSessionService.endSession(sessionId, energyConsumedKWh));
    }
}