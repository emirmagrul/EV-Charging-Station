package com.ev.service.impl;

import com.ev.dto.ChargingSessionDto;
import com.ev.model.ChargingSession;
import com.ev.model.Reservation;
import com.ev.model.enums.ReservationStatus;
import com.ev.model.enums.SessionStatus;
import com.ev.repository.ChargingSessionRepository;
import com.ev.repository.ReservationRepository;
import com.ev.repository.VehicleRepository;
import com.ev.service.IChargerService;
import com.ev.service.IChargingSessionService;
import com.ev.service.IEVDriverService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChargingSessionServiceImpl implements IChargingSessionService {

    private final ChargingSessionRepository chargingSessionRepository;
    private final ReservationRepository reservationRepository;
    private final IEVDriverService evDriverService; // cüzdan ödemesi için
    private final IChargerService chargerService; // Cihaz durumunu güncellemek için
    private final VehicleRepository vehicleRepository;
    private final com.ev.service.INotificationService notificationService;

    @Override
    public ChargingSessionDto getActiveSession(Long driverId) {
        return chargingSessionRepository.findFirstByReservation_Driver_IdAndStatus(driverId, SessionStatus.ACTIVE)
                .map(session -> {
                    ChargingSessionDto dto = new ChargingSessionDto();
                    dto.setId(session.getId());
                    dto.setStartTime(session.getStartTime());
                    dto.setStartPercentage(session.getStartPercentage());
                    dto.setStatus(session.getStatus());
                    dto.setReservationId(session.getReservation().getId());
                    return dto;
                }).orElse(null);
    }

    @Override
    @Transactional
    public ChargingSessionDto startSession(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı!"));

        if (!ReservationStatus.CONFIRMED.equals(reservation.getStatus())) {
            throw new RuntimeException("Hata: Ödemesi yapılmamış veya geçersiz rezervasyonla seans başlatılamaz!");
        }

        // Zaman kontrolü
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resStart = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
        LocalDateTime resEnd = LocalDateTime.of(reservation.getReservationDate(), reservation.getEndTime());

        // Rezervasyondan çok önce başlanamaz
        if (now.isBefore(resStart.minusMinutes(15))) {
            throw new RuntimeException("Hata: Seans henüz başlatılamaz. Rezervasyon saatinize " +
                    java.time.Duration.between(now, resStart).toMinutes() + " dakika var.");
        }
        // Rezervasyon süresi geçmişse başlatılamaz
        if (now.isAfter(resEnd)) {
            throw new RuntimeException("Hata: Rezervasyon süreniz dolmuş. Yeni bir rezervasyon yapmalısınız.");
        }

        // İstasyon şu an başkası tarafından kullanılıyor mu (Önceki müşteri geç çıkmış olabilir) veya arızalı mı?
        if (!com.ev.model.enums.ChargerStatus.AVAILABLE.equals(reservation.getCharger().getStatus())) {
            throw new RuntimeException("Hata: Şarj ünitesi şu an müsait değil (" + reservation.getCharger().getStatus() + "). Önceki kullanıcının işlemi bitirmesi veya cihazın aktif hale gelmesi bekleniyor.");
        }

        // Araç ve başlangıç şarjı simülasyonu
        com.ev.model.Vehicle vehicle = vehicleRepository.findByOwnerId(reservation.getDriver().getId()).stream()
                .filter(v -> v.getConnectorType().getId().equals(reservation.getCharger().getConnectorType().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Uyumlu araç bulunamadı!"));

        // Simülasyon: Kullanıcı aracı sürmüş ve şarjı azalmış varsayıyoruz. 
        // Her yeni seans başladığında batarya %10 ile %40 arasında bir değerde olsun.
        double randomStart = 10.0 + (Math.random() * 30.0);
        randomStart = Math.round(randomStart * 10.0) / 10.0;
        vehicle.setCurrentBatteryPercentage(randomStart);
        vehicleRepository.save(vehicle);

        // Seans başlatma
        ChargingSession session = new ChargingSession();
        session.setReservation(reservation);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartPercentage(vehicle.getCurrentBatteryPercentage());

        chargerService.updateStatus(reservation.getCharger().getId(), com.ev.model.enums.ChargerStatus.OCCUPIED);

        ChargingSession saved = chargingSessionRepository.save(session);

        // Şarj Başladı Bildirimi
        notificationService.sendNotification(
                reservation.getDriver().getId(),
                "Şarj İşlemi Başladı",
                String.format("Aracınız %s istasyonunda %% %.1f şarj seviyesiyle şarj edilmeye başlanmıştır. İyi yolculuklar dileriz.", 
                        reservation.getCharger().getStation().getStationName(), 
                        saved.getStartPercentage()),
                com.ev.model.enums.NotificationType.SYSTEM_ALERT
        );

        ChargingSessionDto dto = new ChargingSessionDto();
        dto.setId(saved.getId());
        dto.setStartTime(saved.getStartTime());
        dto.setStartPercentage(saved.getStartPercentage());
        dto.setStatus(saved.getStatus());
        return dto;
    }

    @Override
    @Transactional
    public ChargingSessionDto endSession(Long sessionId) {
        ChargingSession session = chargingSessionRepository
                .findById(sessionId).orElseThrow(() -> new RuntimeException("Seans bulunamadı!"));

        if (SessionStatus.FINISHED.equals(session.getStatus())) {
            throw new RuntimeException("Hata: Bu seans zaten sonlandırılmış!");
        }

        LocalDateTime now = LocalDateTime.now();
        session.setEndTime(now);
        session.setStatus(SessionStatus.FINISHED);

        // Araç ve enerji hesaplamaları
        com.ev.model.Vehicle vehicle = vehicleRepository.findByOwnerId(session.getReservation().getDriver().getId()).stream()
                .filter(v -> v.getConnectorType().getId().equals(session.getReservation().getCharger().getConnectorType().getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Uyumlu araç bulunamadı!"));

        long minutesPassed = java.time.Duration.between(session.getStartTime(), now).toMinutes();
        if (minutesPassed <= 0) minutesPassed = 1; // En az 1 dakika

        double hoursPassed = minutesPassed / 60.0;
        double chargerPowerKw = session.getReservation().getCharger().getPowerOutput();
        double batteryCapacity = vehicle.getBatteryCapacity();
        double startPct = session.getStartPercentage() != null ? session.getStartPercentage() : 0.0;

        // Cihazın bu sürede basabileceği maksimum enerji
        double maxEnergyByTime = chargerPowerKw * hoursPassed;

        // Aracın 100% olması için gereken maksimum enerji
        double maxEnergyVehicleCanTake = batteryCapacity * ((100.0 - startPct) / 100.0);

        // Gerçekte aktarılan enerji, aracın alabileceğinden fazla olamaz (100% sınırı)
        double actualEnergyConsumed = Math.min(maxEnergyByTime, maxEnergyVehicleCanTake);

        // Yeni Yüzde Hesaplama
        double endPct = startPct + ((actualEnergyConsumed / batteryCapacity) * 100.0);

        // 100% aşmasını engellemek için güvenlik kontrolü
        if (endPct > 100.0) {
            endPct = 100.0;
        }

        // Sürpriz uzun ondalıklar çıkmaması için yuvarlama
        endPct = Math.round(endPct * 10.0) / 10.0;
        actualEnergyConsumed = Math.round(actualEnergyConsumed * 100.0) / 100.0;

        session.setEnergyConsumedKwh(actualEnergyConsumed);
        session.setEndPercentage(endPct);

        // Araç bataryasını güncelle
        vehicle.setCurrentBatteryPercentage(endPct);
        vehicleRepository.save(vehicle);

        // Gerçek maliyeti Hesaplama
        BigDecimal unitPrice = session.getReservation().getCharger().getStation().getPricingPerKWh();
        BigDecimal actualCost = unitPrice.multiply(BigDecimal.valueOf(actualEnergyConsumed));
        session.setTotalCost(actualCost);

        long reservedMinutes = java.time.Duration.between(session.getReservation().getStartTime(),
                session.getReservation().getEndTime()).toMinutes();
        double reservedHours = reservedMinutes / 60.0;
        BigDecimal prepaidAmount = unitPrice.multiply(BigDecimal.valueOf(chargerPowerKw * reservedHours));

        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal extraAmount = BigDecimal.ZERO;
        
        // Eğer gerçek harcama peşin ödenenden azsa, farkı sürücüye iade et
        if (prepaidAmount.compareTo(actualCost) > 0) {
            refundAmount = prepaidAmount.subtract(actualCost);
            evDriverService.addBalance(session.getReservation().getDriver().getId(), refundAmount);
        } else if (actualCost.compareTo(prepaidAmount) > 0) {
            // Eğer gerçek harcama peşin ödenenden fazlaysa (Örn: Erken başlatıp fazladan kullanım yaptıysa)
            extraAmount = actualCost.subtract(prepaidAmount);
            // Eksi bakiye kontrolünü atlayarak borç yazdırmak için addBalance metoduna negatif değer gönderiyoruz
            evDriverService.addBalance(session.getReservation().getDriver().getId(), extraAmount.negate());
        }

        chargingSessionRepository.save(session);

        session.getReservation().setStatus(ReservationStatus.COMPLETED);
        chargerService.updateStatus(session.getReservation().getCharger().getId(),
                com.ev.model.enums.ChargerStatus.AVAILABLE);

        // Bildirim Gönder
        String notificationMessage = String.format("Şarj işleminiz tamamlandı. Aracınıza %.2f kWh enerji aktarılarak şarjınız %% %.1f seviyesine ulaştı. Toplam tutar: %.2f TL.", 
                actualEnergyConsumed, endPct, actualCost);
        
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            notificationMessage += String.format(" Kullanılmayan süreye ait %.2f TL cüzdanınıza iade edilmiştir.", refundAmount);
        } else if (extraAmount.compareTo(BigDecimal.ZERO) > 0) {
            notificationMessage += String.format(" Erken başlama/fazla kullanım sebebiyle oluşan ek %.2f TL cüzdanınızdan tahsil edilmiştir (Gerekirse eksi bakiyeye düşülmüştür).", extraAmount);
        }

        notificationService.sendNotification(
                session.getReservation().getDriver().getId(),
                "Şarj İşlemi Tamamlandı",
                notificationMessage,
                com.ev.model.enums.NotificationType.SYSTEM_ALERT
        );

        ChargingSessionDto responseDto = new ChargingSessionDto();
        responseDto.setId(session.getId());
        responseDto.setStartTime(session.getStartTime());
        responseDto.setEndTime(session.getEndTime());
        responseDto.setEnergyConsumedKwh(session.getEnergyConsumedKwh());
        responseDto.setTotalCost(session.getTotalCost());
        responseDto.setStartPercentage(session.getStartPercentage());
        responseDto.setEndPercentage(session.getEndPercentage());
        responseDto.setStatus(session.getStatus());
        responseDto.setReservationId(session.getReservation().getId());

        return responseDto;
    }
}
