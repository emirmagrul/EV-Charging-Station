package com.ev.service;

import com.ev.dto.StationMapDto;

import java.util.List;

public interface IMapService {
    //Filtrelenmiş ve mesafesi hesaplanmış istasyonları döner
    List<StationMapDto> getStationsForMap(double userLat, double userLng, String connectorType);

    //Seçilen istasyon için Google Maps rotasını hazırlar
    String getNavigationUrl(double userLat, double userLng, Long stationId);
}
