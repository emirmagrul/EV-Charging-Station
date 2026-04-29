package com.ev.service;

import java.math.BigDecimal;
import java.util.Map;

public interface IAdminService {
    BigDecimal getTotalRevenue(); //Toplam Gelir
    long getTotalChargingSessions(); //Toplam Seans Sayısı
    Map<String, Long> getStationUsageStats(); //İstasyon bazlı kullanım oranları
    Map<Integer, Long> getPeakHours(); //En yoğun saatler (Pik saat analizi)
}