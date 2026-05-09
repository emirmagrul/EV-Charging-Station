package com.ev.dto;

import lombok.Data;

@Data
public class RevenueReportDto {
    private java.math.BigDecimal totalNetworkRevenue;
    private java.util.Map<String, java.math.BigDecimal> revenueByStation;
    private java.util.Map<String, Long> sessionCountByStation;    // İstasyon bazlı seans sayısı
    private java.util.Map<String, Double> occupancyRateByStation; // İstasyon doluluk oranı (%)
    private double averageRevenuePerSession;
}
