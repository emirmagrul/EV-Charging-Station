package com.ev.controller;

import com.ev.dto.IssueReportDto;
import com.ev.model.enums.ReportStatus;
import com.ev.service.IIssueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fault-reports")
@RequiredArgsConstructor
public class IssueReportController {

    private final IIssueReportService issueReportService;

    @PostMapping("/report")
    public ResponseEntity<IssueReportDto> reportIssue(@RequestBody IssueReportDto issueReportDto) {
        return ResponseEntity.ok(issueReportService.reportIssue(issueReportDto));
    }

    @GetMapping
    public ResponseEntity<List<IssueReportDto>> getAllFaultReports() {
        return ResponseEntity.ok(issueReportService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueReportDto> getFaultReportById(@PathVariable Long id) {
        return ResponseEntity.ok(issueReportService.findById(id));
    }

    //Operatör raporun durumunu günceller (Örn: Tamir başladı/bitti)
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @RequestParam ReportStatus status) {
        issueReportService.updateReportStatus(id, status);
        return ResponseEntity.ok("Rapor durumu güncellendi.");
    }

    //İstasyon bazlı arıza raporlarını listeler
    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<IssueReportDto>> getReportsByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(issueReportService.getReportsByStation(stationId));
    }

    @GetMapping("/charger/{chargerId}")
    public ResponseEntity<List<IssueReportDto>> getReportsByCharger(@PathVariable Long chargerId) {
        return ResponseEntity.ok(issueReportService.findByChargerId(chargerId));
    }
}