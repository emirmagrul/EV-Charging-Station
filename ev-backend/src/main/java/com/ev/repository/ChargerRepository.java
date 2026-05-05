package com.ev.repository;

import com.ev.model.Charger;
import com.ev.model.enums.ChargerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargerRepository extends JpaRepository<Charger, Long> {
    List<Charger> findByStationId(Long stationId);
    List<Charger> findByStationIdAndStatus(Long stationId, ChargerStatus status); // Müsaitlik durumu sorgulama
}
