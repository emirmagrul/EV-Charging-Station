package com.ev.service.impl;

import com.ev.dto.ChargingStationDto;
import com.ev.dto.EVDriverDto;
import com.ev.model.ChargingStation;
import com.ev.model.EVDriver;
import com.ev.repository.AdminRepository;
import com.ev.repository.ChargingStationRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.StationOperatorRepository;
import com.ev.service.IEVDriverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EVDriverServiceImpl implements IEVDriverService {

    private final EVDriverRepository evDriverRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final StationOperatorRepository operatorRepository;
    private final AdminRepository adminRepository;
    private final com.ev.service.INotificationService notificationService;

    @Override
    @Transactional
    public EVDriverDto createDriver(EVDriverDto evDriverDto) {
        // E-posta kontrolü
        if (evDriverRepository.findByEmail(evDriverDto.getEmail()).isPresent()) {
            throw new RuntimeException("Bu e-posta adresi zaten kullanımda!");
        }

        EVDriver evDriver = new EVDriver();
        evDriver.setFirstName(evDriverDto.getFirstName());
        evDriver.setLastName(evDriverDto.getLastName());
        evDriver.setEmail(evDriverDto.getEmail());
        evDriver.setPassword(evDriverDto.getPassword()); // Şifreyi kaydet

        evDriver.setWalletBalance(
                evDriverDto.getWalletBalance() != null ? evDriverDto.getWalletBalance() : BigDecimal.ZERO);

        // Rolü belirle (boşsa DRIVER varsay)
        com.ev.model.enums.UserRole role = evDriverDto.getRole() != null ? evDriverDto.getRole() : com.ev.model.enums.UserRole.DRIVER;

        if (com.ev.model.enums.UserRole.OPERATOR.equals(role) || com.ev.model.enums.UserRole.ADMIN.equals(role)) {
            throw new RuntimeException("Hata: " + role + " kaydı yanlış servis üzerinden yapılıyor!");
        }

        evDriver.setRole(role);

        EVDriver savedDriver = evDriverRepository.save(evDriver);
        evDriverDto.setId(savedDriver.getId());
        evDriverDto.setRole(role);
        evDriverDto.setPassword(null); // Geriye şifreyi dönme
        return evDriverDto;
    }

    @Override
    public EVDriverDto login(String email, String password, com.ev.model.enums.UserRole requiredRole) {
        log.info("Giriş Denemesi: {} | İstenen Rol: {}", email, requiredRole);

        if (com.ev.model.enums.UserRole.OPERATOR.equals(requiredRole)) {
            com.ev.model.StationOperator operator = operatorRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı!"));

            if (operator.getPassword() == null || !operator.getPassword().equals(password)) {
                throw new RuntimeException("E-posta veya şifre hatalı!");
            }

            EVDriverDto dto = new EVDriverDto();
            dto.setId(operator.getId());
            dto.setFirstName(operator.getFirstName());
            dto.setLastName(operator.getLastName());
            dto.setEmail(operator.getEmail());
            dto.setRole(operator.getRole());
            return dto;
        }

        if (com.ev.model.enums.UserRole.ADMIN.equals(requiredRole)) {
            com.ev.model.Admin admin = adminRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı!"));

            if (admin.getPassword() == null || !admin.getPassword().equals(password)) {
                throw new RuntimeException("E-posta veya şifre hatalı!");
            }

            EVDriverDto dto = new EVDriverDto();
            dto.setId(admin.getId());
            dto.setFirstName(admin.getFirstName());
            dto.setLastName(admin.getLastName());
            dto.setEmail(admin.getEmail());
            dto.setRole(admin.getRole());
            return dto;
        }

        EVDriver driver = evDriverRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-posta veya şifre hatalı!"));

        log.info("Kullanıcı Bulundu: {} | DB Rolü: {}", driver.getEmail(), driver.getRole());

        if (driver.getPassword() == null || !driver.getPassword().equals(password)) {
            throw new RuntimeException("E-posta veya şifre hatalı!");
        }

        // Rol kontrolü: Null güvenli karşılaştırma
        if (driver.getRole() == null || driver.getRole() != requiredRole) {
            log.error("YETKİSİZ GİRİŞ: DB'deki {} rolü {} ile uyuşmuyor!", driver.getRole(), requiredRole);
            throw new RuntimeException("Yetkisiz giriş: Bu hesap için " + requiredRole + " yetkisi bulunamadı!");
        }

        log.info("Giriş Başarılı: {}", email);
        return mapToDto(driver);
    }

    private EVDriverDto mapToDto(EVDriver driver) {
        EVDriverDto dto = new EVDriverDto();

        dto.setId(driver.getId());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setEmail(driver.getEmail());
        dto.setWalletBalance(driver.getWalletBalance());
        dto.setRole(driver.getRole());
        return dto;
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

        // Düşük bakiye kontrolü ve bildirimi (Örn: 100 TL altı)
        if (driver.getWalletBalance().compareTo(new BigDecimal("100")) < 0) {
            notificationService.sendNotification(
                    driver.getId(),
                    "Düşük Bakiye Uyarısı",
                    String.format("Cüzdan bakiyeniz %.2f TL'ye düşmüştür. Kesintisiz şarj deneyimi için lütfen bakiye yükleyiniz.", driver.getWalletBalance()),
                    com.ev.model.enums.NotificationType.WALLET_ALERT
            );
        }
    }

    @Override
    @Transactional
    public EVDriverDto findById(Long id) {
        EVDriver driver = evDriverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));

        EVDriverDto dto = new EVDriverDto();
        dto.setId(driver.getId());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setEmail(driver.getEmail());
        dto.setWalletBalance(driver.getWalletBalance());

        if (driver.getFavoriteStations() != null) {
            List<Long> favoriteStationIds = driver.getFavoriteStations().stream()
                    .map(ChargingStation::getId)
                    .collect(Collectors.toList());
            dto.setFavoriteStationIds(favoriteStationIds);
        }

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
    @Transactional
    public List<ChargingStationDto> getFavoriteStations(Long driverId) {
        EVDriver driver = evDriverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Sürücü bulunamadı!"));

        return driver.getFavoriteStations().stream().map(s -> {
            ChargingStationDto dto = new ChargingStationDto();
            dto.setId(s.getId());
            dto.setStationName(s.getStationName());
            dto.setAddress(s.getAddress());
            dto.setLatitude(s.getLatitude());
            dto.setLongitude(s.getLongitude());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public long getTotalDriverCount() {
        // JPA'nın varsayılan count() metodunu çağırıyoruz
        return evDriverRepository.count();
    }
}