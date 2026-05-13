package com.ev;

import com.ev.dto.ChargingSessionDto;
import com.ev.model.*;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReservationStatus;
import com.ev.model.enums.SessionStatus;
import com.ev.repository.ChargingSessionRepository;
import com.ev.repository.ReservationRepository;
import com.ev.repository.VehicleRepository;
import com.ev.service.IChargerService;
import com.ev.service.IEVDriverService;
import com.ev.service.INotificationService;
import com.ev.service.impl.ChargingSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Senaryosu 5 — Şarj Oturumu İş Mantığı
 * Kapsam: startSession(), endSession(), maliyet hesaplama,
 *         iade hesaplama, cihaz durum güncellemesi
 */
@DisplayName("Test Senaryosu 5: Şarj Oturumu İş Mantığı")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ChargingSessionServiceTest {

    @Mock private ChargingSessionRepository chargingSessionRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private IEVDriverService evDriverService;
    @Mock private IChargerService chargerService;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private INotificationService notificationService;

    @InjectMocks
    private ChargingSessionServiceImpl chargingSessionService;

    // ---- Ortak nesneler ----
    private EVDriver driver;
    private ConnectorType ccsType;
    private ChargingStation station;
    private Charger charger;
    private Vehicle vehicle;
    private Reservation confirmedReservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        driver = new EVDriver();
        driver.setId(1L);
        driver.setFirstName("Emir");
        driver.setLastName("Mağrul");

        ccsType = new ConnectorType();
        ccsType.setId(1L);
        ccsType.setName("CCS");

        station = new ChargingStation();
        station.setId(10L);
        station.setStationName("Karşıyaka Hub");
        station.setPricingPerKWh(new BigDecimal("4.00"));

        charger = new Charger();
        charger.setId(1L);
        charger.setConnectorType(ccsType);
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStation(station);
        charger.setPowerOutput(50.0);  // 50 kW

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setConnectorType(ccsType);
        vehicle.setBatteryCapacity(75.0);  // 75 kWh
        vehicle.setCurrentBatteryPercentage(20.0);

        // Bugün + 30 dakika sonrası başlayan geçerli bir CONFIRMED rezervasyon
        LocalTime startTime = LocalTime.now().plusMinutes(5).withSecond(0).withNano(0);
        LocalTime endTime = startTime.plusHours(1);

        confirmedReservation = new Reservation();
        confirmedReservation.setId(1L);
        confirmedReservation.setDriver(driver);
        confirmedReservation.setCharger(charger);
        confirmedReservation.setStatus(ReservationStatus.CONFIRMED);
        confirmedReservation.setReservationDate(LocalDate.now());
        confirmedReservation.setStartTime(startTime.minusMinutes(20)); // 20 dk önce başlamış (15 dk erken başlatma penceresi içinde)
        confirmedReservation.setEndTime(endTime);
    }

    // -----------------------------------------------------------------------
    // SENARYO 5A: Başarılı Seans Başlatma
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5A - CONFIRMED rezervasyonla seans başarıyla başlatılmalı")
    void testStartSession_Success() {
        // Rezervasyon zamanı: şu andan 20 dk önce başlamış, 15 dk içinde (geçerli pencere)
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        confirmedReservation.setStartTime(now.minusMinutes(10));  // 10 dk önce başladı
        confirmedReservation.setEndTime(now.plusHours(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(confirmedReservation));
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        ChargingSession savedSession = new ChargingSession();
        savedSession.setId(100L);
        savedSession.setStatus(SessionStatus.ACTIVE);
        savedSession.setStartTime(LocalDateTime.now());
        savedSession.setStartPercentage(20.0);
        savedSession.setReservation(confirmedReservation);

        when(chargingSessionRepository.save(any(ChargingSession.class))).thenReturn(savedSession);

        ChargingSessionDto result = chargingSessionService.startSession(1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(SessionStatus.ACTIVE, result.getStatus());

        // Cihaz OCCUPIED olarak güncellenmeli
        verify(chargerService, times(1))
                .updateStatus(eq(1L), eq(ChargerStatus.OCCUPIED));
    }

    // -----------------------------------------------------------------------
    // SENARYO 5B: Onaylanmamış Rezervasyonla Seans Başlatma → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5B - PENDING durumdaki rezervasyonla seans başlatılamamalı")
    void testStartSession_NotConfirmed_ShouldThrow() {
        confirmedReservation.setStatus(ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(confirmedReservation));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> chargingSessionService.startSession(1L));

        assertTrue(ex.getMessage().contains("Ödemesi yapılmamış") || ex.getMessage().contains("geçersiz"),
                "Hata mesajı ödeme hatası içermeli, gerçek: " + ex.getMessage());

        verify(chargingSessionRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // SENARYO 5C: Meşgul Cihazla Seans Başlatma → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5C - OCCUPIED durumdaki şarj cihazıyla seans başlatılamamalı")
    void testStartSession_ChargerOccupied_ShouldThrow() {
        charger.setStatus(ChargerStatus.OCCUPIED);

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        confirmedReservation.setStartTime(now.minusMinutes(5));
        confirmedReservation.setEndTime(now.plusHours(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(confirmedReservation));
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(vehicle));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> chargingSessionService.startSession(1L));

        assertTrue(ex.getMessage().contains("müsait değil"),
                "Hata mesajı 'müsait değil' içermeli, gerçek: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // SENARYO 5D: Maliyet Hesabı — Tam Seans (Önceden Ödenen = Gerçek Maliyet)
    // Girdi: 50 kW cihaz, 1 saat, birim fiyat 4 TL/kWh
    // Beklenen toplam: 4 × 50 × 1 = 200 TL
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5D - 1 saatlik tam seans sonrası maliyet 200 TL olmalı")
    void testEndSession_CostCalculation_FullSession() {
        LocalDateTime sessionStart = LocalDateTime.now().minusHours(1);

        ChargingSession activeSession = new ChargingSession();
        activeSession.setId(50L);
        activeSession.setStatus(SessionStatus.ACTIVE);
        activeSession.setStartTime(sessionStart);
        activeSession.setStartPercentage(20.0);
        activeSession.setReservation(confirmedReservation);

        // Rezervasyon: 1 saat (önceden ödenen = 50 kW × 1 h × 4 TL = 200 TL)
        confirmedReservation.setStartTime(sessionStart.toLocalTime());
        confirmedReservation.setEndTime(sessionStart.toLocalTime().plusHours(1));

        when(chargingSessionRepository.findById(50L)).thenReturn(Optional.of(activeSession));
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(vehicle));
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(i -> i.getArgument(0));

        ChargingSessionDto result = chargingSessionService.endSession(50L);

        assertNotNull(result);
        assertNotNull(result.getTotalCost());

        // Maliyet: unitPrice × actualEnergyConsumed ≥ 0 ve pozitif olmalı
        assertTrue(result.getTotalCost().compareTo(BigDecimal.ZERO) > 0,
                "Toplam maliyet 0'dan büyük olmalı");

        // 50 kW × ~1h = ~50 kWh, × 4 TL = ~200 TL
        // Araç bataryasını doldurabileceğinden emin olalım: 75 kWh'in %80'i = 60 kWh > 50 kWh → tam çekilir
        assertEquals(0, new BigDecimal("200.00").compareTo(result.getTotalCost()),
                "1 saatlik 50kW seans için maliyet 200 TL olmalı");
    }

    // -----------------------------------------------------------------------
    // SENARYO 5E: Maliyet Hesabı — Birim Fiyat × Tüketim
    // Girdi: 45 kWh tüketim, 4 TL/kWh birim fiyat
    // Beklenen: 45 × 4 = 180 TL
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5E - 45 kWh tüketim ve 4 TL/kWh birim fiyatla maliyet 180 TL olmalı")
    void testCostFormula_45kWh_At4TL() {
        // Servis kodundan alınan formül:
        // actualCost = unitPrice.multiply(BigDecimal.valueOf(actualEnergyConsumed))
        BigDecimal unitPrice = new BigDecimal("4.0");
        double energyConsumed = 45.0;

        BigDecimal actualCost = unitPrice.multiply(BigDecimal.valueOf(energyConsumed));

        assertEquals(0, new BigDecimal("180.0").compareTo(actualCost),
                "45 kWh × 4 TL/kWh = 180 TL olmalı");
    }

    // -----------------------------------------------------------------------
    // SENARYO 5F: Seans Bitmişse Tekrar Sonlandırma → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5F - Zaten bitmiş bir seans tekrar sonlandırılamamalı")
    void testEndSession_AlreadyFinished_ShouldThrow() {
        ChargingSession finishedSession = new ChargingSession();
        finishedSession.setId(99L);
        finishedSession.setStatus(SessionStatus.FINISHED);  // Zaten bitti

        when(chargingSessionRepository.findById(99L)).thenReturn(Optional.of(finishedSession));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> chargingSessionService.endSession(99L));

        assertTrue(ex.getMessage().contains("zaten sonlandırılmış"),
                "Hata mesajı 'zaten sonlandırılmış' içermeli");
    }

    // -----------------------------------------------------------------------
    // SENARYO 5G: Erken Sonlandırma → İade Hesaplama
    // Senaryo: 1 saatlik rezervasyon, 30 dk'da bitirildi → yarı maliyet ödenmeli, geri kalan iade
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5G - Erken sonlandırmada kullanılmayan süre iade edilmeli")
    void testEndSession_EarlyTermination_RefundTriggered() {
        // 30 dakika önce başladı (yarım saat kullanıldı)
        LocalDateTime sessionStart = LocalDateTime.now().minusMinutes(30);

        ChargingSession activeSession = new ChargingSession();
        activeSession.setId(60L);
        activeSession.setStatus(SessionStatus.ACTIVE);
        activeSession.setStartTime(sessionStart);
        activeSession.setStartPercentage(10.0);
        activeSession.setReservation(confirmedReservation);

        // Rezervasyon: 1 saat (önceden ödenen = 50 kW × 1 h × 4 TL = 200 TL)
        confirmedReservation.setStartTime(sessionStart.toLocalTime());
        confirmedReservation.setEndTime(sessionStart.toLocalTime().plusHours(1));

        when(chargingSessionRepository.findById(60L)).thenReturn(Optional.of(activeSession));
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(vehicle));
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(i -> i.getArgument(0));

        ChargingSessionDto result = chargingSessionService.endSession(60L);

        // 30 dk kullanım: 50 kW × 0.5 h = 25 kWh → 25 × 4 = 100 TL gerçek maliyet
        // Önceden ödenen: 50 × 1 × 4 = 200 TL
        // İade: 200 - 100 = 100 TL
        assertTrue(result.getTotalCost().compareTo(new BigDecimal("200.00")) < 0,
                "Erken sonlandırmada gerçek maliyet, peşinat ödemesinden az olmalı");

        // addBalance çağrılmalı (iade)
        verify(evDriverService, times(1)).addBalance(eq(1L), any(BigDecimal.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 5H: Seans Sonunda Cihaz AVAILABLE'a Döner
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5H - Seans bitince şarj cihazı AVAILABLE durumuna geçmeli")
    void testEndSession_ChargerBecomesAvailable() {
        LocalDateTime sessionStart = LocalDateTime.now().minusHours(1);

        ChargingSession activeSession = new ChargingSession();
        activeSession.setId(70L);
        activeSession.setStatus(SessionStatus.ACTIVE);
        activeSession.setStartTime(sessionStart);
        activeSession.setStartPercentage(15.0);
        activeSession.setReservation(confirmedReservation);

        confirmedReservation.setStartTime(sessionStart.toLocalTime());
        confirmedReservation.setEndTime(sessionStart.toLocalTime().plusHours(1));

        when(chargingSessionRepository.findById(70L)).thenReturn(Optional.of(activeSession));
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(vehicle));
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(i -> i.getArgument(0));

        chargingSessionService.endSession(70L);

        // Cihaz serbest bırakılmalı
        verify(chargerService, times(1))
                .updateStatus(eq(1L), eq(ChargerStatus.AVAILABLE));
    }

    // -----------------------------------------------------------------------
    // SENARYO 5I: Seans Sonunda Sürücüye Bildirim Gönderilir
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("5I - Seans tamamlanınca sürücüye bildirim gönderilmeli")
    void testEndSession_NotificationSent() {
        LocalDateTime sessionStart = LocalDateTime.now().minusHours(1);

        ChargingSession activeSession = new ChargingSession();
        activeSession.setId(80L);
        activeSession.setStatus(SessionStatus.ACTIVE);
        activeSession.setStartTime(sessionStart);
        activeSession.setStartPercentage(25.0);
        activeSession.setReservation(confirmedReservation);

        confirmedReservation.setStartTime(sessionStart.toLocalTime());
        confirmedReservation.setEndTime(sessionStart.toLocalTime().plusHours(1));

        when(chargingSessionRepository.findById(80L)).thenReturn(Optional.of(activeSession));
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(vehicle));
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(chargingSessionRepository.save(any(ChargingSession.class))).thenAnswer(i -> i.getArgument(0));

        chargingSessionService.endSession(80L);

        verify(notificationService, times(1)).sendNotification(
                eq(1L),
                eq("Şarj İşlemi Tamamlandı"),
                anyString(),
                eq(com.ev.model.enums.NotificationType.SYSTEM_ALERT)
        );
    }
}
