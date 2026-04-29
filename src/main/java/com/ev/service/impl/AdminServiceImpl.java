package com.ev.service.impl;

import com.ev.model.ChargingSession;
import com.ev.repository.ChargingSessionRepository;
import com.ev.service.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {

    private final ChargingSessionRepository sessionRepository;

    @Override
    public BigDecimal getTotalRevenue() {
        return sessionRepository.findAll().stream()
                .map(s -> s.getTotalCost() != null ? s.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long getTotalChargingSessions() {
        return sessionRepository.count();
    }

    @Override
    public Map<String, Long> getStationUsageStats() {
        return sessionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getReservation().getCharger().getStation().getStationName(),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<Integer, Long> getPeakHours() {
        // Seans başlangıç saatlerine göre yoğunluk analizi
        return sessionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartTime().getHour(),
                        Collectors.counting()
                ));
    }
}