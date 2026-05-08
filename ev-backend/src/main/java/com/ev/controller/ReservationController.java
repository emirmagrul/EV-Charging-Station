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

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/charger/{chargerId}")
    public ResponseEntity<List<ReservationDto>> getReservationsByCharger(@PathVariable Long chargerId) {
        return ResponseEntity.ok(reservationService.getByChargerId(chargerId));
    }

    //Ödemeyi tahsil et ve rezervasyonu kesinleştir (Statü: CONFIRMED)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirmReservation(@PathVariable Long id) {
        reservationService.confirmReservation(id);
        return ResponseEntity.ok("Ödeme başarıyla alındı ve rezervasyon onaylandı.");
    }

    // Rezervasyonu iptal et ve ücret iadesi yap
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long id, @RequestParam(required = false) String reason) {
        reservationService.cancelReservation(id, reason);
        return ResponseEntity.ok("Rezervasyon iptal edildi.");
    }

    //Sürücünün kendi rezervasyon geçmişini görmesi
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReservationDto>> getMyReservations(@PathVariable Long driverId) {
        return ResponseEntity.ok(reservationService.getMyReservations(driverId));
    }

    @GetMapping("/charger/{chargerId}/booked-slots")
    public ResponseEntity<List<ReservationDto>> getBookedSlots(
            @PathVariable Long chargerId, 
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(reservationService.getBookedSlots(chargerId, date));
    }
}