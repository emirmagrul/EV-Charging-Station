package com.ev.dto;

import lombok.Data;

@Data
public class ChargerDto {
    private Long id;
    private double powerOutput;
    private String status;
    private ConnecterTypeDto connectorType;
    private Long stationId;
}
