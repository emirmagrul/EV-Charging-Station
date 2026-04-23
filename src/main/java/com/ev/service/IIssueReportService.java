package com.ev.service;

import com.ev.dto.IssueReportDto;
import com.ev.model.enums.ReportStatus;

import java.util.List;

public interface IIssueReportService {
    IssueReportDto reportIssue(IssueReportDto issueReportDto);
    void updateReportStatus(Long reportId, ReportStatus newStatus);
    List<IssueReportDto> getReportsByStation(Long stationId);
}
