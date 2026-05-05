package com.ev.dto;

import com.ev.model.enums.ReportStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueReportDto {
    private Long id;
    private String description;
    private LocalDateTime reportedAt;
    private ReportStatus status;
    private Long driverId;
    private Long chargerId;
    private String chargerPower;
    private String stationName;
}
