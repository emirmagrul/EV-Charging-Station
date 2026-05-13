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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Senaryosu 2 & 3 — Rezervasyon İş Mantığı
 * Kapsam: makeReservation() içindeki tüm validasyon katmanları:
 *   - Araç–Şarj ünitesi soket uyumluluğu
 *   - Zaman dilimi çakışma kontrolü
 *   - Bakımdaki cihaza rezervasyon engeli
 *   - Süre sınırı (maks 2 saat) kontrolü
 *   - 24 saat öncesi rezervasyon yasağı
 */
@DisplayName("Test Senaryosu 2-3: Rezervasyon İş Mantığı")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private EVDriverRepository evDriverRepository;
    @Mock private ChargerRepository chargerRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private IEVDriverService evDriverService;
    @Mock private INotificationService notificationService;
    @Mock private IssueReportRepository issueReportRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    // ---- Ortak nesneler ----
    private EVDriver driver;
    private ConnectorType ccsType;
    private ConnectorType chademoType;
    private ChargingStation station;
    private Charger availableCcsCharger;

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

        chademoType = new ConnectorType();
        chademoType.setId(2L);
        chademoType.setName("CHAdeMO");

        station = new ChargingStation();
        station.setId(10L);
        station.setStationName("Karşıyaka Hub");
        station.setOperatingHours("24/7");

        availableCcsCharger = new Charger();
        availableCcsCharger.setId(1L);
        availableCcsCharger.setConnectorType(ccsType);
        availableCcsCharger.setStatus(ChargerStatus.AVAILABLE);
        availableCcsCharger.setStation(station);
        availableCcsCharger.setPowerOutput(50.0);

        // Varsayılan mock'lar — üzerinden yazmak mümkün
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(chargerRepository.findById(1L)).thenReturn(Optional.of(availableCcsCharger));
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any(), any(), any()))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(new Reservation());
    }

    /**
     * Geçerli bir rezervasyon isteği oluşturur.
     *
     * Gece yarısı geçişi koruması: LocalTime.Duration.between() startTime > endTime
     * olduğunda negatif sonuç üretir ("2 saati aşamaz" hatasını tetikler).
     * Bu yüzden her iki saatin de aynı takvim gününde olduğunu garanti ediyoruz.
     *
     * - Saat 22:00'dan önce  → bugün, now+1h → now+2h (aynı gün, ≤24h)
     * - Saat 22:00 veya sonra → yarın 10:00–11:00 (now>22:00 iken yarın 10:00 < now+24h garantisi var)
     */
    private ReservationDto buildValidRequest(Long chargerId) {
        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        if (now.isAfter(LocalTime.of(21, 59))) {
            // 22:00+ → endTime gece yarısını aşar; yarının sabahını kullan
            // Garanti: now >= 22:00 → now+24h >= 22:00+1gün > yarın 10:00 ✓
            date      = LocalDate.now().plusDays(1);
            startTime = LocalTime.of(10, 0);
            endTime   = LocalTime.of(11, 0);
        } else {
            // 00:00-21:59 → now+1h ve now+2h aynı takvim günündedir
            date      = LocalDate.now();
            startTime = now.plusHours(1);
            endTime   = now.plusHours(2);
        }

        ReservationDto dto = new ReservationDto();
        dto.setDriverId(1L);
        dto.setChargerId(chargerId);
        dto.setReservationDate(date);
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        return dto;
    }

    // -----------------------------------------------------------------------
    // SENARYO 2A: Uyumlu Araç ve Şarj Cihazı → Rezervasyon Başarılı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("2A - Soket tipleri eşleştiğinde rezervasyon başarıyla oluşturulmalı")
    void testMakeReservation_CompatibleVehicle_Success() {
        Vehicle ccsVehicle = new Vehicle();
        ccsVehicle.setConnectorType(ccsType);

        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(ccsVehicle));

        ReservationDto request = buildValidRequest(1L);

        // Beklenen: Hiçbir exception fırlatılmamalı
        assertDoesNotThrow(() -> reservationService.makeReservation(request));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 2B: Uyumsuz Araç Soketi → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("2B - Soket tipleri uyuşmadığında rezervasyon reddedilmeli")
    void testMakeReservation_IncompatibleConnector_ShouldThrow() {
        // Şarj cihazı CCS ama araç CHAdeMO
        Charger chademoCharger = new Charger();
        chademoCharger.setId(2L);
        chademoCharger.setConnectorType(chademoType);
        chademoCharger.setStatus(ChargerStatus.AVAILABLE);
        chademoCharger.setStation(station);
        chademoCharger.setPowerOutput(50.0);

        Vehicle ccsVehicle = new Vehicle();
        ccsVehicle.setConnectorType(ccsType);   // Araç CCS

        when(chargerRepository.findById(2L)).thenReturn(Optional.of(chademoCharger));
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(ccsVehicle));

        ReservationDto request = buildValidRequest(2L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(request));

        assertTrue(ex.getMessage().contains("uyumlu değil"),
                "Hata mesajı 'uyumlu değil' içermeli, gerçek mesaj: " + ex.getMessage());

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 3A: Çakışan Rezervasyon → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("3A - Çakışan zaman dilimine rezervasyon yapılamamalı")
    void testMakeReservation_OverlappingSlot_ShouldThrow() {
        Vehicle ccsVehicle = new Vehicle();
        ccsVehicle.setConnectorType(ccsType);

        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(ccsVehicle));

        // Çakışma simülasyonu: mevcut rezervasyon var
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any(), any(), any()))
                .thenReturn(true);

        ReservationDto request = buildValidRequest(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(request));

        assertTrue(ex.getMessage().contains("zaman dilimi dolu"),
                "Hata mesajı 'zaman dilimi dolu' içermeli, gerçek mesaj: " + ex.getMessage());

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 3B: Çakışma Olmayan Farklı Zaman Dilimine Rezervasyon → Başarılı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("3B - Çakışmayan farklı zaman dilimine rezervasyon başarıyla yapılabilmeli")
    void testMakeReservation_NoOverlap_Success() {
        Vehicle ccsVehicle = new Vehicle();
        ccsVehicle.setConnectorType(ccsType);

        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(ccsVehicle));
        // Çakışma yok
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any(), any(), any()))
                .thenReturn(false);

        ReservationDto request = buildValidRequest(1L);

        assertDoesNotThrow(() -> reservationService.makeReservation(request));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 3C: Bakımda Olan Cihaza Rezervasyon → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("3C - OFFLINE durumdaki şarj cihazına rezervasyon yapılamamalı")
    void testMakeReservation_OfflineCharger_ShouldThrow() {
        Charger offlineCharger = new Charger();
        offlineCharger.setId(3L);
        offlineCharger.setConnectorType(ccsType);
        offlineCharger.setStatus(ChargerStatus.OFFLINE);    // Bakımda!
        offlineCharger.setStation(station);
        offlineCharger.setPowerOutput(50.0);

        when(chargerRepository.findById(3L)).thenReturn(Optional.of(offlineCharger));

        ReservationDto request = buildValidRequest(3L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(request));

        assertTrue(ex.getMessage().contains("bakımda"),
                "Hata mesajı 'bakımda' içermeli, gerçek mesaj: " + ex.getMessage());

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 3D: Rezervasyon Süresi 2 Saati Aşıyor → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("3D - 2 saati aşan rezervasyon reddedilmeli")
    void testMakeReservation_ExceedsMaxDuration_ShouldThrow() {
        ReservationDto request = new ReservationDto();
        request.setDriverId(1L);
        request.setChargerId(1L);
        request.setReservationDate(LocalDate.now());

        // 3 saatlik pencere → max 2 saat kuralını ihlal eder
        LocalTime startTime = LocalTime.now().plusHours(1).withSecond(0).withNano(0);
        request.setStartTime(startTime);
        request.setEndTime(startTime.plusHours(3));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(request));

        assertTrue(ex.getMessage().contains("2 saati"),
                "Hata mesajı '2 saati' içermeli, gerçek mesaj: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // SENARYO 3E: Geçmişe Rezervasyon Yapma Girişimi → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("3E - Geçmiş tarih/saate rezervasyon yapılamamalı")
    void testMakeReservation_PastTime_ShouldThrow() {
        ReservationDto request = new ReservationDto();
        request.setDriverId(1L);
        request.setChargerId(1L);
        // Dün = kesinlikle geçmiş
        request.setReservationDate(LocalDate.now().minusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(11, 0));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(request));

        assertTrue(ex.getMessage().contains("Geçmiş") || ex.getMessage().contains("geçmiş"),
                "Hata mesajı geçmiş tarih uyarısı içermeli, gerçek mesaj: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // SENARYO 3F: Araç Sahibinin Hiç Aracı Yok → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("3F - Uyumlu aracı bulunmayan sürücü rezervasyon yapamamalı")
    void testMakeReservation_NoVehicleOwned_ShouldThrow() {
        // Sürücünün hiç aracı yok
        when(vehicleRepository.findByOwnerId(1L)).thenReturn(Collections.emptyList());

        ReservationDto request = buildValidRequest(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reservationService.makeReservation(request));

        assertTrue(ex.getMessage().contains("uyumlu değil"),
                "Hata mesajı 'uyumlu değil' içermeli, gerçek mesaj: " + ex.getMessage());
    }
}
