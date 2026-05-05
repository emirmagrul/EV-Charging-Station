package com.ev.controller;

import com.ev.dto.ReservationDto;
import com.ev.service.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    //Rezervasyon oluşturma (Statü: PENDING)
    @PostMapping("/make")
    public ResponseEntity<ReservationDto> makeReservation(@RequestBody ReservationDto reservationDto) {
        return ResponseEntity.ok(reservationService.makeReservation(reservationDto));
    }

    //Ödemeyi tahsil et ve rezervasyonu kesinleştir (Statü: CONFIRMED)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirmReservation(@PathVariable Long id) {
        reservationService.confirmReservation(id);
        return ResponseEntity.ok("Ödeme başarıyla alındı ve rezervasyon onaylandı.");
    }

    // Rezervasyonu iptal et ve ücret iadesi yap
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.ok("Rezervasyon iptal edildi, ücret cüzdanınıza iade edildi.");
    }

    //Sürücünün kendi rezervasyon geçmişini görmesi
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReservationDto>> getMyReservations(@PathVariable Long driverId) {
        return ResponseEntity.ok(reservationService.getMyReservations(driverId));
    }
}