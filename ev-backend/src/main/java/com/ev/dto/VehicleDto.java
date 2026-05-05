package com.ev.dto;

import lombok.Data;

@Data
public class VehicleDto {
    private Long id;
    private String brand;
    private String model;
    private double batteryCapacity;
    private String plateNumber;
    private Long connectorTypeId;
    private String connectorTypeName;
    private Long driverId;
}
