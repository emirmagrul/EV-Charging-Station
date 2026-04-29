package com.ev.service.impl;

import com.ev.dto.StationMapDto;
import com.ev.model.ChargingStation;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.StationStatus;
import com.ev.repository.ChargingStationRepository;
import com.ev.service.IMapService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapServiceImpl implements IMapService {

    private final ChargingStationRepository stationRepository;

    @Override
    @Transactional
    public List<StationMapDto> getStationsForMap(double userLat, double userLng, String connectorType) {
        return stationRepository.findAll().stream()
                .map(station -> {
                    StationMapDto dto = new StationMapDto();
                    dto.setId(station.getId());
                    dto.setStationName(station.getStationName());
                    dto.setLatitude(station.getLatitude());
                    dto.setLongitude(station.getLongitude());

                    //Kuş uçuşu mesafe hesaplama
                    double distance = calculateHaversine(userLat, userLng, station.getLatitude(), station.getLongitude());
                    dto.setDistance(distance);

                    if (!station.getChargers().isEmpty()) {
                        dto.setConnectorType(station.getChargers().get(0).getConnectorType().getName());
                    }

                    //İstasyonun anlık durumunu belirleme (Color Coding)
                    dto.setStatus(determineStationStatus(station));

                    return dto;
                })
                //Filtreleme Mantığı (Soket tipi veya mesafe sınırı)
                .filter(dto -> connectorType == null || dto.getConnectorType().equalsIgnoreCase(connectorType))
                .sorted(Comparator.comparingDouble(StationMapDto::getDistance)) // En yakını en üste al
                .collect(Collectors.toList());
    }

    @Override
    public String getNavigationUrl(double userLat, double userLng, Long stationId) {
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("İstasyon bulunamadı!"));

        //Google Maps Directions API URL oluşturma
        return String.format("https://www.google.com/maps/dir/?api=1&origin=%f,%f&destination=%f,%f&travelmode=driving",
                userLat, userLng, station.getLatitude(), station.getLongitude());
    }

    //Renk Belirleme İş Mantığı
    private StationStatus determineStationStatus(ChargingStation station) {
        boolean hasAvailable = station.getChargers().stream()
                .anyMatch(c -> c.getStatus() == ChargerStatus.AVAILABLE);

        boolean isAllOffline = station.getChargers().stream()
                .allMatch(c -> c.getStatus() == ChargerStatus.OFFLINE);

        if (isAllOffline) {
            return StationStatus.OFFLINE; // Red
        }
        if (hasAvailable) {
            return StationStatus.AVAILABLE; // Green
        }
        return StationStatus.OCCUPIED; // Yellow
    }

    //Haversine Formülü (Sadece öneriler için tahmini mesafe hesaplıyoruz, istasyon seçildiğinde api ile mesafe ve tarif)
    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Dünya yarıçapı (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}