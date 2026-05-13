package com.ev.service.impl;

import com.ev.dto.IssueReportDto;
import com.ev.model.Charger;
import com.ev.model.IssueReport;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReportStatus;
import com.ev.repository.ChargerRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.IssueReportRepository;
import com.ev.repository.ReservationRepository;
import com.ev.repository.StationOperatorRepository;
import com.ev.service.IChargerService;
import com.ev.service.INotificationService;
import com.ev.service.IIssueReportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueReportServiceImpl implements IIssueReportService {

    private final IssueReportRepository issueReportRepository;
    private final ChargerRepository chargerRepository;
    private final EVDriverRepository evDriverRepository;
    private final StationOperatorRepository stationOperatorRepository;
    private final IChargerService chargerService;
    private final INotificationService notificationService;
    private final ReservationRepository reservationRepository;


    @Override
    @Transactional
    public IssueReportDto reportIssue(IssueReportDto issueReportDto) {
        log.info("Arıza Raporu Alındı: {}", issueReportDto);

        if (issueReportDto.getChargerId() == null) {
            throw new RuntimeException("Cihaz ID bilgisi boş olamaz!");
        }

        // Aynı rezervasyon için daha önce bildirim yapılmış mı kontrol et
        if (issueReportDto.getReservationId() != null) {
            if (issueReportRepository.existsByRelatedReservationId(issueReportDto.getReservationId())) {
                throw new RuntimeException("Bu rezervasyon için daha önce zaten bir geri bildirim gönderilmiş!");
            }
        }

        IssueReport report = new IssueReport();
        report.setDescription(issueReportDto.getDescription());

        // Sürücü veya Operatör tespiti
        if (issueReportDto.getDriverId() != null) {
            evDriverRepository.findById(issueReportDto.getDriverId()).ifPresent(report::setReporter);
        }

        if (issueReportDto.getOperatorId() != null) {
            stationOperatorRepository.findById(issueReportDto.getOperatorId()).ifPresent(report::setOperatorReporter);
        }

        // Eğer ikisi de yoksa ve bir ID geldiyse (Fallback: Operatör olarak aramayı dene)
        if (report.getReporter() == null && report.getOperatorReporter() == null) {
            Long potentialId = issueReportDto.getDriverId() != null ? issueReportDto.getDriverId() : issueReportDto.getOperatorId();
            if (potentialId != null) {
                stationOperatorRepository.findById(potentialId).ifPresent(report::setOperatorReporter);
            }
        }

        Charger charger = chargerRepository.findById(issueReportDto.getChargerId())
                .orElseThrow(() -> new RuntimeException("Cihaz bulunamadı!"));
        report.setTargetCharger(charger);

        // Rezervasyon bağlantısı
        if (issueReportDto.getReservationId() != null) {
            reservationRepository.findById(issueReportDto.getReservationId()).ifPresent(report::setRelatedReservation);
        }

        IssueReport saved = issueReportRepository.save(report);

        // Operatöre Bildirim Gönder (Eğer istasyonun bir operatörü varsa)
        if (charger.getStation().getResponsibleOperator() != null) {
            Long opId = charger.getStation().getResponsibleOperator().getId();
            log.info("Operatöre bildirim gönderiliyor. OperatorID: {}, Station: {}", opId, charger.getStation().getStationName());
            notificationService.sendOperatorNotification(
                    opId,
                    "Yeni Arıza Bildirimi",
                    String.format("%s istasyonundaki %s ünitesi için yeni bir arıza bildirildi: %s",
                            charger.getStation().getStationName(), charger.getId(), report.getDescription()),
                    com.ev.model.enums.NotificationType.SYSTEM_ALERT
            );
        } else {
            log.warn("İstasyonun sorumlu operatörü bulunamadı! Bildirim gönderilemedi. Station: {}", charger.getStation().getStationName());
        }

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
    public List<IssueReportDto> findAll() {
        return issueReportRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IssueReportDto findById(Long id) {
        return issueReportRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Rapor bulunamadı!"));
    }

    @Override
    @Transactional
    public List<IssueReportDto> getReportsByStation(Long stationId) {
        return issueReportRepository.findByTargetChargerStationId(stationId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<IssueReportDto> findByChargerId(Long chargerId) {
        return issueReportRepository.findByTargetChargerId(chargerId).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    // YENİ: Admin Paneli için Sadece Çözülmemiş Arızaları Getir
    @Override
    @Transactional
    public List<IssueReportDto> getUnresolvedReports() {
        return issueReportRepository.findAll().stream()
                .filter(r -> !ReportStatus.RESOLVED.equals(r.getStatus()) && !ReportStatus.DISMISSED.equals(r.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private IssueReportDto mapToDto(IssueReport report) {
        IssueReportDto dto = new IssueReportDto();
        dto.setId(report.getId());
        dto.setDescription(report.getDescription());
        dto.setReportedAt(report.getReportedAt());
        dto.setStatus(report.getStatus());
        dto.setDriverId(report.getReporter() != null ? report.getReporter().getId() : null);
        dto.setOperatorId(report.getOperatorReporter() != null ? report.getOperatorReporter().getId() : null);

        if (report.getReporter() != null) {
            dto.setReporterName(report.getReporter().getFirstName() + " " + report.getReporter().getLastName());
        } else if (report.getOperatorReporter() != null) {
            dto.setReporterName(report.getOperatorReporter().getFirstName() + " " + report.getOperatorReporter().getLastName() + " (Operatör)");
        } else {
            dto.setReporterName("Bilinmeyen");
        }

        dto.setChargerId(report.getTargetCharger().getId());

        // UI tarafında kolaylık için istasyon ve cihaz bilgisini de ekleyelim
        if (report.getTargetCharger().getStation() != null) {
            dto.setStationName(report.getTargetCharger().getStation().getStationName());
        }
        dto.setChargerPower(report.getTargetCharger().getPowerOutput() + "kW");

        return dto;
    }
}