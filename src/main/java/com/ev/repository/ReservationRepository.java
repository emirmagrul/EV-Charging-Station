package com.ev.repository;

import com.ev.model.Reservation;
import com.ev.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByDriverId(Long driverId); // Sürücünün geçmişi [cite: 114]

    // R11: Aynı şarj ünitesi ve zaman dilimi için çakışma kontrolü sorgusu [cite: 104]
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.charger.id = :chargerId " +
            "AND r.reservationDate = :date " +
            "AND r.status IN :statuses " + // Parametreye çevirdik
            "AND (:startTime < r.endTime AND :endTime > r.startTime)")
    boolean existsOverlappingReservation(@Param("chargerId") Long chargerId,
                                         @Param("date") LocalDate date,
                                         @Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime,
                                         @Param("statuses") List<ReservationStatus> statuses);

    List<Reservation> findByChargerIdAndStatus(Long chargerId, ReservationStatus status);

    List<Reservation> findByChargerIdAndStatusAndReservationDateGreaterThanEqual(
            Long chargerId, ReservationStatus status, LocalDate date);

    // 10 dakikadan eski ve hala PENDING olan rezervasyonları bulmak için
    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime dateTime);
}
