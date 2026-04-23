package com.ev.service.impl;

import com.ev.dto.ChargingStationDto;
import com.ev.dto.EVDriverDto;
import com.ev.model.ChargingStation;
import com.ev.model.EVDriver;
import com.ev.repository.ChargingStationRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.service.IEVDriverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EVDriverServiceImpl implements IEVDriverService {

    private final EVDriverRepository evDriverRepository;
    private final ChargingStationRepository chargingStationRepository;

    @Override
    @Transactional
    public EVDriverDto createDriver(EVDriverDto evDriverDto) {
        EVDriver evDriver = new EVDriver();
        evDriver.setFirstName(evDriverDto.getFirstName());
        evDriver.setLastName(evDriverDto.getLastName());
        evDriver.setEmail(evDriverDto.getEmail());

        evDriver.setWalletBalance(evDriverDto.getWalletBalance() != null ?
                                  evDriverDto.getWalletBalance() : BigDecimal.ZERO);

        EVDriver savedDriver = evDriverRepository.save(evDriver);
        evDriverDto.setId(savedDriver.getId());
        return evDriverDto;
    }

    @Override
    @Transactional
    public void addBalance(Long driverId, BigDecimal amount) {
        EVDriver driver = evDriverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));
        driver.setWalletBalance(driver.getWalletBalance().add(amount));
        evDriverRepository.save(driver);
    }

    @Override
    @Transactional
    public void deductBalance(Long driverId, BigDecimal amount) {
        EVDriver driver = evDriverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));

        if (driver.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Yetersiz bakiye!");
        }

        driver.setWalletBalance(driver.getWalletBalance().subtract(amount));
        evDriverRepository.save(driver);
    }

    @Override
    public EVDriverDto findById(Long id) {
        EVDriver driver = evDriverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));

        EVDriverDto dto = new EVDriverDto();
        dto.setId(driver.getId());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setEmail(driver.getEmail());
        dto.setWalletBalance(driver.getWalletBalance());
        return dto;
    }

    @Override
    @Transactional
    public void addStationToFavorites(Long driverId, Long stationId) {
        EVDriver driver = evDriverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));
        ChargingStation station = chargingStationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("İstasyon bulunamadı!"));

        if (!driver.getFavoriteStations().contains(station)) {
            driver.getFavoriteStations().add(station);
            evDriverRepository.save(driver);
        }
    }

    @Override
    @Transactional
    public void removeStationFromFavorites(Long driverId, Long stationId) {
        EVDriver driver = evDriverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));

        driver.getFavoriteStations().removeIf(s -> s.getId().equals(stationId));
        evDriverRepository.save(driver);
    }

    @Override
    public List<ChargingStationDto> getFavoriteStations(Long driverId) {
        EVDriver driver = evDriverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));

        return driver.getFavoriteStations().stream().map(s -> {
            ChargingStationDto dto = new ChargingStationDto();
            dto.setId(s.getId());
            dto.setStationName(s.getStationName());
            dto.setAddress(s.getAddress());
            return dto;
        }).collect(Collectors.toList());
    }
}
