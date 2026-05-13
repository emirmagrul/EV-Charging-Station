package com.ev.repository;

import com.ev.model.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {

    // YENİ: Java Stream yerine direkt DB'de topla (O(1) hızında çalışır)
    @Query("SELECT SUM(s.totalCost) FROM ChargingSession s")
    java.math.BigDecimal calculateTotalRevenue();

    // YENİ: İstasyon bazlı gelir ve seans sayısı dağılımı
    @Query("SELECT s.reservation.charger.station.stationName, SUM(s.totalCost), COUNT(s) " +
            "FROM ChargingSession s " +
            "GROUP BY s.reservation.charger.station.stationName")
    List<Object[]> getUsageStatsByStation();

    // YENİ: Yoğun saat analizi (Peak Hours)
    @Query("SELECT FUNCTION('HOUR', s.startTime), COUNT(s) FROM ChargingSession s GROUP BY FUNCTION('HOUR', s.startTime)")
    List<Object[]> getPeakHourStats();

    @Query("SELECT FUNCTION('HOUR', s.startTime), COUNT(s) " +
           "FROM ChargingSession s " +
           "WHERE s.reservation.charger.station.id = :stationId " +
           "GROUP BY FUNCTION('HOUR', s.startTime)")
    List<Object[]> getPeakHourStatsByStation(Long stationId);

    long countByStartTimeAfterAndStatus(java.time.LocalDateTime startTime, com.ev.model.enums.SessionStatus status);

    java.util.Optional<ChargingSession> findFirstByReservation_Driver_IdAndStatus(Long driverId, com.ev.model.enums.SessionStatus status);

    java.util.Optional<ChargingSession> findByReservationId(Long reservationId);
}
