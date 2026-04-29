package com.ev.dto;

import com.ev.model.enums.StationStatus;
import lombok.Data;

@Data
public class StationMapDto {
    private Long id;
    private String stationName;
    private double latitude;
    private double longitude;
    private StationStatus status; //Renk kodu için
    private double distance;      //Kullanıcıya mesafe
    private String connectorType;
}
