package com.ev.dto;

import lombok.Data;

@Data
public class SystemHealthDto {
    private long totalStations;
    private long activeStations; // En az bir ünitesi AVAILABLE olanlar
    private long totalChargers;
    private long outOfServiceChargers; // OFFLINE veya OUT_OF_SERVICE olanlar
    private long pendingIssueReports; // RESOLVED olmayan arıza kayıtları
}
