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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final ChargingSessionRepository sessionRepository;
    private final ChargingStationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final EVDriverRepository driverRepository;
    private final ReservationRepository reservationRepository;
    private final IssueReportRepository issueReportRepository;

    @Override
    public RevenueReportDto getRevenueReport() {
        RevenueReportDto dto = new RevenueReportDto();

        // 1. Genel toplam gelir
        BigDecimal total = sessionRepository.calculateTotalRevenue();
        dto.setTotalNetworkRevenue(total != null ? total : BigDecimal.ZERO);

        // 2. İstasyon bazlı veriler (Gelir ve Seans Sayısı)
        List<Object[]> usageData = sessionRepository.getUsageStatsByStation();
        Map<String, BigDecimal> revenueMap = new HashMap<>();
        Map<String, Long> sessionMap = new HashMap<>();
        
        for (Object[] row : usageData) {
            String stationName = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Long count = (Long) row[2];
            
            revenueMap.put(stationName, revenue);
            sessionMap.put(stationName, count);
        }
        dto.setRevenueByStation(revenueMap);
        dto.setSessionCountByStation(sessionMap);

        // 3. Doluluk Oranı Hesaplama (Seans Sayısı / İstasyon Kapasitesi oranı)
        List<Object[]> capacityData = stationRepository.getChargerCountsByStation();
        Map<String, Double> occupancyMap = new HashMap<>();
        
        for (Object[] row : capacityData) {
            String stationName = (String) row[0];
            Long chargerCount = (Long) row[1];
            Long sessionCount = sessionMap.getOrDefault(stationName, 0L);

            if (chargerCount > 0) {
                // Basit bir yoğunluk endeksi: Seans Sayısı / Cihaz Sayısı
                // Gerçek dünya için bu değer normalize edilebilir (Örn: / 100)
                double rate = (sessionCount.doubleValue() / chargerCount.doubleValue()) * 10; 
                occupancyMap.put(stationName, Math.min(rate, 100.0)); // Max %100 olsun
            }
        }
        dto.setOccupancyRateByStation(occupancyMap);

        // 4. Seans başına ortalama gelir
        long totalSessions = sessionRepository.count();
        if (totalSessions > 0) {
            dto.setAverageRevenuePerSession(dto.getTotalNetworkRevenue()
                    .divide(BigDecimal.valueOf(totalSessions), 2, RoundingMode.HALF_UP).doubleValue());
        }

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
        List<Object[]> peakData = sessionRepository.getPeakHourStats();
        Map<Integer, Long> peakMap = new HashMap<>();

        for (Object[] row : peakData) {
            // SQL bazen Integer bazen Short dönebilir, güvenli cast yapıyoruz
            Integer hour = ((Number) row[0]).intValue();
            Long count = (Long) row[1];
            peakMap.put(hour, count);
        }
        return peakMap;
    }

    @Override
    public SystemHealthDto getSystemHealthStatus() {
        SystemHealthDto dto = new SystemHealthDto();

        dto.setTotalStations(stationRepository.count());
        dto.setTotalChargers(chargerRepository.count());

        // HIZLI: DB seviyesinde sayım
        dto.setOutOfServiceChargers(chargerRepository.countByStatusIn(
                List.of(ChargerStatus.OFFLINE, ChargerStatus.OUT_OF_SERVICE)));

        // Aktif İstasyon Sayısı (En az bir AVAILABLE ünitesi olan)
        dto.setActiveStations(stationRepository.countActiveStations());

        // Çözülmemiş Arıza Raporları (RESOLVED olmayan her şey)
        dto.setPendingIssueReports(issueReportRepository.countByStatusNot(ReportStatus.RESOLVED));

        return dto;
    }
}