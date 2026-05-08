package com.ev.repository;

import com.ev.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingStationRepository extends JpaRepository<ChargingStation, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT s FROM ChargingStation s WHERE s.responsibleOperator.id = :operatorId")
    List<ChargingStation> findByResponsibleOperatorId(@org.springframework.data.repository.query.Param("operatorId") Long operatorId);
}
