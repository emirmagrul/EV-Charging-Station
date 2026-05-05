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

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanupTask {

    private final ReservationRepository reservationRepository;

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
}
