package com.ev.dto;

import com.ev.model.enums.SessionStatus;
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
    private SessionStatus status;
    private Long reservationId;
}
