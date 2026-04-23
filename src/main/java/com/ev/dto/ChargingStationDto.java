package com.ev.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargingStationDto {
    private Long id;
    private String stationName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String operatingHours;
    private BigDecimal pricingPerKWh;
}
