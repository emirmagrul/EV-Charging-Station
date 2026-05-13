package com.ev;

import com.ev.dto.ReservationDto;
import com.ev.model.*;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReservationStatus;
import com.ev.repository.*;
import com.ev.service.IEVDriverService;
import com.ev.service.INotificationService;
import com.ev.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Senaryosu 6 — Rezervasyon Onaylama ve İptal İş Mantığı
 * Kapsam:
 *   - confirmReservation(): bakiye düşümü, durum geçişi, bildirim
 *   - cancelReservation(): 60 dk penceresi, iade mantığı, operatör iptali
 */
@DisplayName("Test Senaryosu 6: Rezervasyon Onay ve İptal")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ReservationConfirmCancelTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private EVDriverRepository evDriverRepository;
    @Mock private ChargerRepository chargerRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private IEVDriverService evDriverService;
    @Mock private INotificationService notificationService;
    @Mock private IssueReportRepository issueReportRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private EVDriver driver;
    private ChargingStation station;
    private Charger charger;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        driver = new EVDriver();
        driver.setId(1L);
        driver.setFirstName("Emir");
        driver.setLastName("Mağrul");
        driver.setWalletBalance(new BigDecimal("500.00"));

        station = new ChargingStation();
        station.setId(10L);
        station.setStationName("Karşıyaka Hub");
        station.setPricingPerKWh(new BigDecimal("4.00"));

        charger = new Charger();
        charger.setId(1L);
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStation(station);
        charger.setPowerOutput(50.0);  // 50 kW
    }

    /** Yarından 2 saat sonraki geçerli bir PENDING rezervasyon döndürür */
    private Reservation buildPendingReservation(LocalTime start, LocalTime end) {
        Reservation res = new Reservation();
        res.setId(1L);
        res.setDriver(driver);
        res.setCharger(charger);
        res.setStatus(ReservationStatus.PENDING);
        res.setReservationDate(LocalDate.now().plusDays(1)); // yarın
        res.setStartTime(start);
        res.setEndTime(end);
        return res;
    }

    // -----------------------------------------------------------------------
    // SENARYO 6A: Başarılı Rezervasyon Onayı
    // Girdi: PENDING rezervasyon, 1 saat, 50 kW, 4 TL/kWh
    // Beklenen: Bakiyeden 200 TL düşülür, status=CONFIRMED, bildirim gider
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("6A - PENDING rezervasyon onaylandığında bakiye düşülmeli ve status CONFIRMED olmalı")
    void testConfirmReservation_Success() {
        Reservation res = buildPendingReservation(LocalTime.of(10, 0), LocalTime.of(11, 0));
        // 50 kW × 1h × 4 TL = 200 TL tahmini maliyet

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);

        reservationService.confirmReservation(1L);

        // Bakiyeden 200 TL düşülmeli
        // Not: BigDecimal scale bağımsız karşılaştırma yapıyoruz.
        // Servis: 4.00 × valueOf(50.0 × 1.0) = 200.000 (scale=3); eq() scale'i de eşleştirir.
        verify(evDriverService, times(1))
                .deductBalance(eq(1L),
                        argThat(amount -> new BigDecimal("200").compareTo(amount) == 0));

        // Status CONFIRMED olmalı
        assertEquals(ReservationStatus.CONFIRMED, res.getStatus());

        // Onay bildirimi gönderilmeli
        verify(notificationService, times(1)).sendNotification(
                eq(1L), eq("Rezervasyon Onaylandı"), anyString(), any()
        );
    }

    // -----------------------------------------------------------------------
    // SENARYO 6B: Zaten Onaylanmış Rezervasyonu Tekrar Onaylama → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("6B - CONFIRMED durumundaki rezervasyon tekrar onaylanamamalı")
    void testConfirmReservation_AlreadyConfirmed_ShouldThrow() {
        Reservation res = buildPendingReservation(LocalTime.of(10, 0), LocalTime.of(11, 0));
        res.setStatus(ReservationStatus.CONFIRMED);  // Zaten onaylı

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.confirmReservation(1L));

        assertTrue(ex.getMessage().contains("PENDING") || ex.getMessage().contains("bekleyen"),
                "Hata mesajı PENDING beklentisini belirtmeli, gerçek: " + ex.getMessage());

        // Bakiye hiç düşürülmemeli
        verify(evDriverService, never()).deductBalance(anyLong(), any());
    }

    // -----------------------------------------------------------------------
    // SENARYO 6C: Başarılı Sürücü İptali (CONFIRMED → İade + CANCELLED)
    // Girdi: İptal başvurusu rezervasyon saatinden 2+ saat önce
    // Beklenen: 200 TL iade, status=CANCELLED, bildirim
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("6C - CONFIRMED rezervasyon 60+ dk önce iptal edilince iade yapılmalı")
    void testCancelReservation_Confirmed_WithRefund() {
        // Rezervasyon: yarın 10:00-11:00 (çok daha sonra → 60 dk penceresini geçiyor)
        Reservation res = buildPendingReservation(LocalTime.of(10, 0), LocalTime.of(11, 0));
        res.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);

        // reason=null → sürücü kendisi iptal ediyor
        reservationService.cancelReservation(1L, null);

        // Ödenen tutar iade edilmeli: 50kW × 1h × 4TL = 200 TL
        // Not: BigDecimal scale bağımsız karşılaştırma — 200.000 == 200 (compareTo)
        verify(evDriverService, times(1))
                .addBalance(eq(1L),
                        argThat(amount -> new BigDecimal("200").compareTo(amount) == 0));

        assertEquals(ReservationStatus.CANCELLED, res.getStatus());

        verify(notificationService, times(1)).sendNotification(
                eq(1L), anyString(), anyString(), any()
        );
    }

    // -----------------------------------------------------------------------
    // SENARYO 6D: PENDING Rezervasyon İptali (İade Yok)
    // PENDING durumda henüz bakiyeden para çekilmemiştir
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("6D - PENDING rezervasyon iptal edilince iade yapılmamalı")
    void testCancelReservation_Pending_NoRefund() {
        Reservation res = buildPendingReservation(LocalTime.of(10, 0), LocalTime.of(11, 0));
        res.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);

        reservationService.cancelReservation(1L, null);

        // PENDING'de para çekilmediği için iade yok
        verify(evDriverService, never()).addBalance(anyLong(), any());

        assertEquals(ReservationStatus.CANCELLED, res.getStatus());
    }

    // -----------------------------------------------------------------------
    // SENARYO 6E: 60 Dakika Kala İptal Girişimi → Exception
    // Girdi: Rezervasyon başlangıcına 30 dk kalmış
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("6E - Rezervasyona 60 dakikadan az kaldığında iptal yapılamamalı")
    void testCancelReservation_TooLate_ShouldThrow() {
        // Rezervasyon bugün, 30 dk sonrası başlıyor → 60 dk penceresinin içinde
        LocalTime startTime = LocalTime.now().plusMinutes(30).withSecond(0).withNano(0);
        LocalTime endTime   = startTime.plusHours(1);

        Reservation res = new Reservation();
        res.setId(2L);
        res.setDriver(driver);
        res.setCharger(charger);
        res.setStatus(ReservationStatus.CONFIRMED);
        res.setReservationDate(LocalDate.now());   // bugün!
        res.setStartTime(startTime);
        res.setEndTime(endTime);

        when(reservationRepository.findById(2L)).thenReturn(Optional.of(res));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation(2L, null));

        assertTrue(ex.getMessage().contains("1 saatten az") || ex.getMessage().contains("iptal"),
                "60 dk penceresi ihlal mesajı bekleniyor, gerçek: " + ex.getMessage());

        // İptal edilmemeli, iade yok
        verify(evDriverService, never()).addBalance(anyLong(), any());
        verify(reservationRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // SENARYO 6F: Operatör Tarafından İptal (reason dolu → CANCELLED_BY_OPERATOR)
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("6F - Operatör sebep belirterek iptal edince status CANCELLED_BY_OPERATOR olmalı")
    void testCancelReservation_ByOperator_WithReason() {
        Reservation res = buildPendingReservation(LocalTime.of(14, 0), LocalTime.of(15, 0));
        res.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(res);

        reservationService.cancelReservation(1L, "İstasyon bakıma alınacak");

        assertEquals(ReservationStatus.CANCELLED_BY_OPERATOR, res.getStatus());

        // Operatör iptalinde de iade yapılmalı (CONFIRMED idi)
        verify(evDriverService, times(1)).addBalance(eq(1L), any(BigDecimal.class));

        // Operatör bildirimi gönderilmeli
        verify(notificationService, times(1)).sendNotification(
                eq(1L),
                eq("Rezervasyonunuz İptal Edildi"),
                argThat(msg -> msg.contains("İstasyon bakıma alınacak")),
                any()
        );
    }

    // -----------------------------------------------------------------------
    // SENARYO 6G: Zaten İptal Edilmiş Rezervasyonu Tekrar İptal Etme → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("6G - Zaten CANCELLED durumundaki rezervasyon tekrar iptal edilememeli")
    void testCancelReservation_AlreadyCancelled_ShouldThrow() {
        Reservation res = buildPendingReservation(LocalTime.of(10, 0), LocalTime.of(11, 0));
        res.setStatus(ReservationStatus.CANCELLED);  // Zaten iptal

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.cancelReservation(1L, null));

        assertTrue(ex.getMessage().contains("onaylanmış") || ex.getMessage().contains("bekleyen"),
                "Geçersiz durum mesajı bekleniyor, gerçek: " + ex.getMessage());
    }
}
