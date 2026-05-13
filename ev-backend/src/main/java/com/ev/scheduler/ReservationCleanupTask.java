package com.ev.scheduler;

import com.ev.model.Reservation;
import com.ev.model.enums.ReservationStatus;
import com.ev.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import com.ev.repository.ChargingSessionRepository;
import com.ev.service.IChargingSessionService;
import com.ev.service.INotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupTask {

    private final ReservationRepository reservationRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final IChargingSessionService chargingSessionService;
    private final INotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredPendingReservations() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndCreatedAtBefore(ReservationStatus.PENDING, tenMinutesAgo);

        if (!expiredReservations.isEmpty()) {
            for (Reservation res : expiredReservations) {
                res.setStatus(ReservationStatus.CANCELLED);
                log.info("Zaman aşımı nedeniyle rezervasyon iptal edildi ID: {}", res.getId());
            }
            reservationRepository.saveAll(expiredReservations);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelNoShowReservations() {
        // Rezervasyon saatinin üzerinden 15 dakika geçmiş olan CONFIRMED rezervasyonları bul
        List<Reservation> noShowCandidates = reservationRepository.findNoShowCandidates(
                java.time.LocalDate.now(),
                java.time.LocalTime.now().minusMinutes(15));

        if (!noShowCandidates.isEmpty()) {
            for (Reservation res : noShowCandidates) {
                // Eğer bu rezervasyon için herhangi bir şarj seansı BAŞLAMAMIŞSA (No-Show)
                boolean hasSession = chargingSessionRepository.findByReservationId(res.getId()).isPresent();

                if (!hasSession) {
                    res.setStatus(ReservationStatus.CANCELLED);
                    log.info("No-Show: Müşteri 15 dakika içinde gelmediği için rezervasyon iptal edildi (Para iadesi YOK) ID: {}", res.getId());

                    // Müşteriye ceza bildirimi gönder
                    notificationService.sendNotification(
                            res.getDriver().getId(),
                            "Rezervasyon İptali (No-Show)",
                            String.format("%s istasyonundaki rezervasyonunuza 15 dakika geciktiğiniz için işleminiz iptal edilmiştir. Kurallar gereği ücret iadesi yapılamamaktadır.",
                                    res.getCharger().getStation().getStationName()),
                            com.ev.model.enums.NotificationType.SYSTEM_ALERT
                    );
                }
            }
            reservationRepository.saveAll(noShowCandidates);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void completeFinishedReservations() {
        List<Reservation> finished = reservationRepository.findFinishedReservations(
                java.time.LocalDate.now(),
                java.time.LocalTime.now());

        if (!finished.isEmpty()) {
            for (Reservation res : finished) {
                // Eğer bu rezervasyona ait başlatılmış ve hala ACTIVE olan bir seans varsa onu otomatik bitir.
                var activeSessionOpt = chargingSessionRepository.findByReservationId(res.getId())
                        .filter(s -> com.ev.model.enums.SessionStatus.ACTIVE.equals(s.getStatus()));

                if (activeSessionOpt.isPresent()) {
                    log.info("Hayalet Seans Tespit Edildi! Rezervasyon süresi bittiği için seans zorla sonlandırılıyor: ID={}", activeSessionOpt.get().getId());
                    try {
                        chargingSessionService.endSession(activeSessionOpt.get().getId());
                    } catch (Exception e) {
                        log.error("Hayalet seans sonlandırılırken hata: ", e);
                    }
                } else {
                    // Sadece seans başlatmamış olanların durumunu COMPLETED yap
                    if (res.getStatus() != ReservationStatus.COMPLETED) {
                        res.setStatus(ReservationStatus.COMPLETED);
                        log.info("Rezervasyon bittiği için tamamlandı olarak işaretlendi (No-Show veya zaten bitmiş) ID: {}", res.getId());
                    }
                }
            }
            reservationRepository.saveAll(finished);
        }
    }
}
