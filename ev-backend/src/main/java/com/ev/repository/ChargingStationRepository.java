package com.ev.repository;

import com.ev.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    @Query("SELECT s FROM ChargingStation s WHERE s.responsibleOperator.id = :operatorId")
    List<ChargingStation> findByResponsibleOperatorId(@Param("operatorId") Long operatorId);

    @Query("SELECT COUNT(DISTINCT s) FROM ChargingStation s JOIN s.chargers c WHERE c.status = com.ev.model.enums.ChargerStatus.AVAILABLE")
    long countActiveStations();

    @Query("SELECT s.stationName, COUNT(c) FROM ChargingStation s JOIN s.chargers c GROUP BY s.stationName")
    List<Object[]> getChargerCountsByStation();
}