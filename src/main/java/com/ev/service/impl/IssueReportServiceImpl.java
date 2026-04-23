package com.ev.service.impl;

import com.ev.dto.IssueReportDto;
import com.ev.model.Charger;
import com.ev.model.EVDriver;
import com.ev.model.IssueReport;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReportStatus;
import com.ev.repository.ChargerRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.IssueReportRepository;
import com.ev.service.IChargerService;
import com.ev.service.IIssueReportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueReportServiceImpl implements IIssueReportService {

    private final IssueReportRepository issueReportRepository;
    private final ChargerRepository chargerRepository;
    private final EVDriverRepository evDriverRepository;
    private final IChargerService chargerService;


    @Override
    @Transactional
    public IssueReportDto reportIssue(IssueReportDto issueReportDto) {
        IssueReport report = new IssueReport();
        report.setDescription(issueReportDto.getDescription());

        EVDriver driver = evDriverRepository.findById(issueReportDto.getDriverId())
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));
        report.setReporter(driver);

        Charger charger = chargerRepository.findById(issueReportDto.getChargerId())
                .orElseThrow(() -> new RuntimeException("Cihaz bulunamadı!"));
        report.setTargetCharger(charger);

        IssueReport  saved = issueReportRepository.save(report);
        issueReportDto.setId(saved.getId());
        issueReportDto.setReportedAt(saved.getReportedAt());
        issueReportDto.setStatus(saved.getStatus());
        return issueReportDto;
    }

    @Override
    @Transactional
    public void updateReportStatus(Long reportId, ReportStatus newStatus) {
        IssueReport report = issueReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Rapor bulunamadı!"));

        report.setStatus(newStatus);
        issueReportRepository.save(report);

        //Tamir başladığı an cihazı kapatma
        if (ReportStatus.IN_PROGRESS.equals(newStatus)) {
            chargerService.updateStatus(report.getTargetCharger().getId(), ChargerStatus.OFFLINE);
        }

        //Tamir bittiği an cihazı açma
        if (ReportStatus.RESOLVED.equals(newStatus)) {
            chargerService.updateStatus(report.getTargetCharger().getId(), ChargerStatus.AVAILABLE);
        }
    }

    @Override
    @Transactional
    public List<IssueReportDto> getReportsByStation(Long stationId) {
        return issueReportRepository.findByTargetChargerStationId(stationId).stream()
                .map(report -> {
                    IssueReportDto dto = new IssueReportDto();
                    dto.setId(report.getId());
                    dto.setDescription(report.getDescription());
                    dto.setReportedAt(report.getReportedAt());
                    dto.setStatus(report.getStatus());
                    dto.setDriverId(report.getReporter().getId());
                    dto.setChargerId(report.getTargetCharger().getId());

                    // UI tarafında kolaylık için istasyon ve cihaz bilgisini de ekleyelim
                    dto.setStationName(report.getTargetCharger().getStation().getStationName());
                    dto.setChargerPower(report.getTargetCharger().getPowerOutput() + "kW");

                    return dto;
                }).collect(Collectors.toList());
    }
}
