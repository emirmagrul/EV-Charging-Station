package com.ev.dto;

import com.ev.model.enums.ChargerStatus;
import lombok.Data;

@Data
public class ChargerDto {
    private Long id;
    private double powerOutput;
    private ChargerStatus status;
    private ConnectorTypeDto connectorType;
    private Long stationId;
    private String stationName;       // Operatör paneli için
    private Double pricePerKwh;       // İstasyonun kWh fiyatı (frontend: charger.pricePerKwh)
}
