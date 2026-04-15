package com.ev.repository;

import com.ev.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByDriverId(Long driverId); // Sürücünün geçmişi [cite: 114]

    // R11: Aynı şarj ünitesi ve zaman dilimi için çakışma kontrolü sorgusu [cite: 104]
    List<Reservation> findByChargerIdAndReservationDateAndStatus(Long chargerId, LocalDate date, String status);
}
