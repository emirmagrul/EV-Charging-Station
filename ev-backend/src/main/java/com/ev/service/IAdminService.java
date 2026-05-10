package com.ev.service;

import com.ev.dto.RevenueReportDto;
import com.ev.dto.SystemHealthDto;
import com.ev.dto.UserActivityDto;

import java.util.Map;

public interface IAdminService {
    // 1. İdari Raporlama: Gelir ve istasyon bazlı analizler
    RevenueReportDto getRevenueReport();

    // 2. Kullanıcı Faaliyet Özetleri
    UserActivityDto getUserActivitySummary();

    // 3. Ağ Performansı: Yoğun saat ve kullanım analizi
    Map<Integer, Long> getPeakHourAnalysis();

    // 4. Sistem Sağlığı ve Denetimi
    SystemHealthDto getSystemHealthStatus();

    // 5. Kayıt
    com.ev.model.Admin save(com.ev.model.Admin admin);

    // 6. İstasyon Bazlı Yoğun Saat Analizi
    Map<Integer, Long> getPeakHourAnalysisByStation(Long stationId);
}