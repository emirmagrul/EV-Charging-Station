package com.ev.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargingSessionDto {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double energyConsumedKwh;
    private BigDecimal totalCost;
    private String status;
    private Long reservationId;
}
