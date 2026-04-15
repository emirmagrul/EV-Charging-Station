package com.ev.repository;

import com.ev.model.Charger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByStationIdAndStatus(Long stationId, String status); // Müsaitlik durumu sorgulama [cite: 99]
}
