package com.ev.dto;

import lombok.Data;

@Data
public class RevenueReportDto {
    private java.math.BigDecimal totalNetworkRevenue;
    private java.util.Map<String, java.math.BigDecimal> revenueByStation;
    private java.util.Map<String, java.math.BigDecimal> pendingRevenueByStation; // Beklenen (Rezervasyon) Gelir
    private java.util.Map<String, Long> sessionCountByStation; // ŞU AN AKTİF OLANLAR
    private java.util.Map<String, Long> historicalUsageCountByStation; // TÜM ZAMANLAR (KART İÇİN)
    private java.util.Map<String, Long> reservationCountByStation; // Aktif Rezervasyon Sayısı
    private java.util.Map<String, Double> occupancyRateByStation;
    private double averageRevenuePerSession;
}
