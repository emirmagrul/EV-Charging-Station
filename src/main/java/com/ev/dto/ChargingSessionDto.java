package com.ev.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargingSessionDto {
    private Long id;
    private double energyConsumedKwh;
    private BigDecimal totalCost;
    private Long reservationId;
}
