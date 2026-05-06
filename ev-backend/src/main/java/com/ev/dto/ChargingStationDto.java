package com.ev.dto;

import com.ev.model.enums.ChargerStatus;
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
    private java.util.List<Long> supportedConnectorTypeIds;
    private ChargerStatus status;
}

