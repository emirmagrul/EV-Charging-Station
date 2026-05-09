package com.ev.service;

import com.ev.dto.IssueReportDto;
import com.ev.model.enums.ReportStatus;

import java.util.List;

public interface IIssueReportService {
    IssueReportDto reportIssue(IssueReportDto issueReportDto);
    void updateReportStatus(Long reportId, ReportStatus newStatus);
    List<IssueReportDto> findAll();
    IssueReportDto findById(Long id);
    List<IssueReportDto> getReportsByStation(Long stationId);
    List<IssueReportDto> findByChargerId(Long chargerId);
    List<IssueReportDto> getUnresolvedReports();
}