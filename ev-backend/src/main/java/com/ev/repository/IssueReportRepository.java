package com.ev.repository;

import com.ev.model.IssueReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {
    List<IssueReport> findByTargetChargerId(Long chargerId);

    List<IssueReport> findByTargetChargerStationId(Long stationId);
}
