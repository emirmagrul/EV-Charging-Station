package com.ev.repository;

import com.ev.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    List<ChargingStation> findByStationNameContainingIgnoreCase(String name); // İstasyon arama [cite: 98]
}
