package com.ev.service.impl;

import com.ev.dto.RevenueReportDto;
import com.ev.dto.SystemHealthDto;
import com.ev.dto.UserActivityDto;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReportStatus;
import com.ev.model.enums.ReservationStatus;
import com.ev.repository.*;
import com.ev.service.IAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ev.model.Reservation;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingStationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final EVDriverRepository driverRepository;
    private final ReservationRepository reservationRepository;
    private final IssueReportRepository issueReportRepository;
    private final AdminRepository adminRepository;

    @Override
    public RevenueReportDto getRevenueReport() {
        RevenueReportDto dto = new RevenueReportDto();
        
        // 1. Gelir Verileri (Historical + Pending)
        BigDecimal historicalRevenue = sessionRepository.calculateTotalRevenue();
        if (historicalRevenue == null) historicalRevenue = BigDecimal.ZERO;
        
        List<Reservation> allReservations = reservationRepository.findAll();
        BigDecimal completedResRevenue = BigDecimal.ZERO;
        BigDecimal pendingResRevenue = BigDecimal.ZERO;
        
        Map<String, BigDecimal> stationRevenueMap = new HashMap<>();
        Map<String, BigDecimal> stationPendingMap = new HashMap<>();
        Map<String, Long> activeResMap = new HashMap<>();
        Map<String, Long> inProgressSessionMap = new HashMap<>();
        Map<String, Long> historicalUsageMap = new HashMap<>();

        // SADECE ŞU AN AKTİF OLAN SEANSLARI (ACTIVE) SAY
        List<com.ev.model.ChargingSession> currentSessions = sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() == com.ev.model.enums.SessionStatus.ACTIVE)
                .collect(Collectors.toList());
        for(com.ev.model.ChargingSession s : currentSessions) {
            if (s.getReservation() != null && s.getReservation().getCharger() != null) {
                String name = s.getReservation().getCharger().getStation().getStationName();
                inProgressSessionMap.put(name, inProgressSessionMap.getOrDefault(name, 0L) + 1);
            }
        }

        // SEANS TABLOSUNDAKİ GEÇMİŞ VERİLERİ DE SAY
        List<Object[]> sessionStats = sessionRepository.getUsageStatsByStation();
        if (sessionStats != null) {
            for (Object[] row : sessionStats) {
                String name = (String) row[0];
                Long count = (Long) row[2];
                historicalUsageMap.put(name, historicalUsageMap.getOrDefault(name, 0L) + count);
            }
        }

        LocalDateTime now = LocalDateTime.now();

        for (Reservation res : allReservations) {
            String sName = res.getCharger().getStation().getStationName();
            long minutes = Duration.between(res.getStartTime(), res.getEndTime()).toMinutes();
            double hours = minutes / 60.0;
            BigDecimal estVal = res.getCharger().getStation().getPricingPerKWh()
                    .multiply(BigDecimal.valueOf(res.getCharger().getPowerOutput() * hours));

            if (res.getStatus() == ReservationStatus.COMPLETED) {
                completedResRevenue = completedResRevenue.add(estVal);
                stationRevenueMap.put(sName, stationRevenueMap.getOrDefault(sName, BigDecimal.ZERO).add(estVal));
                historicalUsageMap.put(sName, historicalUsageMap.getOrDefault(sName, 0L) + 1);
            } else if (res.getStatus() == ReservationStatus.PENDING || res.getStatus() == ReservationStatus.CONFIRMED) {
                pendingResRevenue = pendingResRevenue.add(estVal);
                stationPendingMap.put(sName, stationPendingMap.getOrDefault(sName, BigDecimal.ZERO).add(estVal));
                activeResMap.put(sName, activeResMap.getOrDefault(sName, 0L) + 1);
            }
        }

        dto.setTotalNetworkRevenue(historicalRevenue.add(completedResRevenue));
        dto.setRevenueByStation(stationRevenueMap);
        dto.setPendingRevenueByStation(stationPendingMap);
        dto.setSessionCountByStation(inProgressSessionMap); 
        dto.setHistoricalUsageCountByStation(historicalUsageMap); // TÜM ZAMANLAR
        dto.setReservationCountByStation(activeResMap);

        // 2. Zaman Slotu Bazlı Doluluk Oranı (Bugün + Yarın)
        List<Object[]> capacityData = stationRepository.getChargerCountsByStation();
        Map<String, Double> occupancyMap = new HashMap<>();
        
        for (Object[] row : capacityData) {
            String sName = (String) row[0];
            Long chargerCount = (Long) row[1];
            
            // İstasyonu bul (Çalışma saatleri için)
            com.ev.model.ChargingStation station = stationRepository.findAll().stream()
                    .filter(s -> s.getStationName().equals(sName)).findFirst().orElse(null);
            
            if (station != null && chargerCount > 0) {
                int dailyOperatingHours = 14; 
                String opHours = station.getOperatingHours();
                int openH = 8, closeH = 22;

                if (opHours != null && opHours.contains("-")) {
                    try {
                        String[] parts = opHours.split("-");
                        openH = Integer.parseInt(parts[0].split(":")[0]);
                        closeH = Integer.parseInt(parts[1].split(":")[0]);
                        dailyOperatingHours = closeH - openH;
                    } catch (Exception e) {}
                } else if ("24/7".equals(opHours)) {
                    dailyOperatingHours = 24; openH = 0; closeH = 24;
                }

                // Bugün kalan saatler (Şu an ile kapanış arası)
                int currentH = now.getHour();
                int remainingToday = Math.max(0, closeH - Math.max(currentH, openH));
                
                // Toplam Slot Kapasitesi = (Bugün Kalan + Yarın Tüm) * Cihaz Sayısı
                double totalAvailableHourSlots = (remainingToday + dailyOperatingHours) * chargerCount.doubleValue();
                
                // Bu istasyona ait aktif rezervasyonların toplam saat süresi
                double reservedHours = allReservations.stream()
                        .filter(r -> r.getCharger().getStation().getStationName().equals(sName))
                        .filter(r -> r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED)
                        .mapToDouble(r -> Duration.between(r.getStartTime(), r.getEndTime()).toMinutes() / 60.0)
                        .sum();

                if (totalAvailableHourSlots > 0) {
                    double rate = (reservedHours / totalAvailableHourSlots) * 100.0;
                    occupancyMap.put(sName, Math.min(rate, 100.0));
                } else {
                    occupancyMap.put(sName, 0.0);
                }
            }
        }
        dto.setOccupancyRateByStation(occupancyMap);

        return dto;
    }

    @Override
    public UserActivityDto getUserActivitySummary() {
        UserActivityDto dto = new UserActivityDto();

        dto.setTotalDrivers(driverRepository.count());

        // HIZLI: DB seviyesinde sayım
        long activeRes = reservationRepository.countByStatusIn(
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));
        dto.setActiveReservations(activeRes);

        // Bugün tamamlanan seanslar (Start time'ı bugün olan ve COMPLETED olanlar)
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        dto.setCompletedSessionsToday(sessionRepository.countByStartTimeAfterAndStatus(
                startOfToday, com.ev.model.enums.SessionStatus.FINISHED));

        return dto;
    }

    @Override
    public Map<Integer, Long> getPeakHourAnalysis() {
        Map<Integer, Long> peakMap = new HashMap<>();
        for (int i = 0; i < 24; i++) peakMap.put(i, 0L);

        // 1. Seanslar
        try {
            List<Object[]> peakData = sessionRepository.getPeakHourStats();
            if (peakData != null) {
                for (Object[] row : peakData) {
                    if (row[0] != null) {
                        int hour = ((Number) row[0]).intValue();
                        long count = ((Number) row[1]).longValue();
                        peakMap.put(hour, peakMap.get(hour) + count);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Global peak stats error: {}", e.getMessage());
        }

        // 2. Rezervasyonlar
        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation res : reservations) {
            if (res.getStartTime() != null) {
                int hour = res.getStartTime().getHour();
                peakMap.put(hour, peakMap.get(hour) + 1);
            }
        }
        return peakMap;
    }

    @Override
    public Map<Integer, Long> getPeakHourAnalysisByStation(Long stationId) {
        Map<Integer, Long> peakMap = new HashMap<>();
        for (int i = 0; i < 24; i++) peakMap.put(i, 0L);

        // 1. İstasyon Seansları
        try {
            List<Object[]> stats = sessionRepository.getPeakHourStatsByStation(stationId);
            if (stats != null) {
                for (Object[] row : stats) {
                    if (row[0] != null) {
                        int hour = ((Number) row[0]).intValue();
                        long count = ((Number) row[1]).longValue();
                        peakMap.put(hour, peakMap.get(hour) + count);
                    }
                }
            }
        } catch (Exception e) {}

        // 2. İstasyon Rezervasyonları
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> r.getCharger() != null && 
                             r.getCharger().getStation() != null && 
                             r.getCharger().getStation().getId().equals(stationId))
                .collect(Collectors.toList());
                
        for (Reservation res : reservations) {
            if (res.getStartTime() != null) {
                int hour = res.getStartTime().getHour();
                peakMap.put(hour, peakMap.get(hour) + 1);
            }
        }
        return peakMap;
    }

    @Override
    public SystemHealthDto getSystemHealthStatus() {
        SystemHealthDto dto = new SystemHealthDto();
        dto.setTotalStations(stationRepository.count());
        dto.setTotalChargers(chargerRepository.count());
        dto.setOutOfServiceChargers(chargerRepository.countByStatusIn(
                List.of(ChargerStatus.OFFLINE, ChargerStatus.OUT_OF_SERVICE)));
        dto.setActiveStations(stationRepository.countActiveStations());
        dto.setPendingIssueReports(issueReportRepository.countByStatusNot(ReportStatus.RESOLVED));
        return dto;
    }

    @Override
    public com.ev.model.Admin save(com.ev.model.Admin admin) {
        return adminRepository.save(admin);
    }
}