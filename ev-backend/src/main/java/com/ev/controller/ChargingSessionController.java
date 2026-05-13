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

    //Sürücünün aktif seansını getirir
    @GetMapping("/active/{driverId}")
    public ResponseEntity<ChargingSessionDto> getActiveSession(@PathVariable Long driverId) {
        ChargingSessionDto activeSession = chargingSessionService.getActiveSession(driverId);
        if (activeSession == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(activeSession);
    }

    //Şarj seansını başlatır (Cihaz durumunu OCCUPIED yapar)
    @PostMapping("/start/{reservationId}")
    public ResponseEntity<ChargingSessionDto> startSession(@PathVariable Long reservationId) {
        return ResponseEntity.ok(chargingSessionService.startSession(reservationId));
    }

    //Şarjı bitirir, tüketimi hesaplar ve gerekirse iade yapar
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ChargingSessionDto> endSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chargingSessionService.endSession(sessionId));
    }
}