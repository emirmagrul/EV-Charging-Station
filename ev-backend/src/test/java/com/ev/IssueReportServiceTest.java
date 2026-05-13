package com.ev;

import com.ev.dto.IssueReportDto;
import com.ev.model.*;
import com.ev.model.enums.ChargerStatus;
import com.ev.model.enums.ReportStatus;
import com.ev.repository.*;
import com.ev.service.IChargerService;
import com.ev.service.INotificationService;
import com.ev.service.impl.IssueReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test Senaryosu 7 — Arıza Raporlama İş Mantığı
 * Kapsam:
 *   - reportIssue(): başarılı bildirim, mükerrer bildirim engeli, cihaz ID zorunluluğu
 *   - updateReportStatus(): tamir başlayınca OFFLINE, tamamlanınca AVAILABLE
 *   - Operatöre bildirim yönlendirmesi
 */
@DisplayName("Test Senaryosu 7: Arıza Raporlama")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class IssueReportServiceTest {

    @Mock private IssueReportRepository issueReportRepository;
    @Mock private ChargerRepository chargerRepository;
    @Mock private EVDriverRepository evDriverRepository;
    @Mock private StationOperatorRepository stationOperatorRepository;
    @Mock private IChargerService chargerService;
    @Mock private INotificationService notificationService;
    @Mock private ReservationRepository reservationRepository;

    @InjectMocks
    private IssueReportServiceImpl issueReportService;

    private EVDriver driver;
    private StationOperator operator;
    private ChargingStation station;
    private Charger charger;
    private IssueReport savedReport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        driver = new EVDriver();
        driver.setId(1L);
        driver.setFirstName("Emir");
        driver.setLastName("Mağrul");

        operator = new StationOperator();
        operator.setId(5L);
        operator.setFirstName("Ahmet");
        operator.setLastName("Operatör");

        station = new ChargingStation();
        station.setId(10L);
        station.setStationName("Karşıyaka Hub");
        station.setResponsibleOperator(operator);

        charger = new Charger();
        charger.setId(1L);
        charger.setStatus(ChargerStatus.AVAILABLE);
        charger.setStation(station);
        charger.setPowerOutput(50.0);

        savedReport = new IssueReport();
        savedReport.setId(100L);
        savedReport.setDescription("Kablo çalışmıyor");
        savedReport.setTargetCharger(charger);
        savedReport.setStatus(ReportStatus.OPEN);
    }

    // -----------------------------------------------------------------------
    // SENARYO 7A: Başarılı Arıza Bildirimi
    // Girdi: Geçerli chargerId, geçerli driverId, açıklama
    // Beklenen: Rapor kaydedilir, operatöre bildirim gider
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7A - Geçerli girişlerle arıza bildirimi başarıyla kaydedilmeli")
    void testReportIssue_Success() {
        IssueReportDto inputDto = new IssueReportDto();
        inputDto.setChargerId(1L);
        inputDto.setDriverId(1L);
        inputDto.setDescription("Kablo çalışmıyor");

        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger));
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(issueReportRepository.save(any(IssueReport.class))).thenReturn(savedReport);

        IssueReportDto result = issueReportService.reportIssue(inputDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());

        // Rapor DB'ye kaydedilmeli
        verify(issueReportRepository, times(1)).save(any(IssueReport.class));

        // Operatöre bildirim gönderilmeli (istasyonun sorumlu operatörü var)
        verify(notificationService, times(1)).sendOperatorNotification(
                eq(5L), eq("Yeni Arıza Bildirimi"), anyString(), any()
        );
    }

    // -----------------------------------------------------------------------
    // SENARYO 7B: Cihaz ID Eksik → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7B - Cihaz ID olmadan arıza bildirimi yapılamamalı")
    void testReportIssue_NullChargerId_ShouldThrow() {
        IssueReportDto inputDto = new IssueReportDto();
        inputDto.setChargerId(null);  // Eksik!
        inputDto.setDriverId(1L);
        inputDto.setDescription("Sorun var");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> issueReportService.reportIssue(inputDto));

        assertTrue(ex.getMessage().contains("boş olamaz"),
                "Hata mesajı 'boş olamaz' içermeli, gerçek: " + ex.getMessage());

        verify(issueReportRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // SENARYO 7C: Aynı Rezervasyon için İkinci Bildirim → Exception
    // Girdi: reservationId=55 için daha önce rapor açılmış
    // Beklenen: "daha önce zaten bir geri bildirim" hatası
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7C - Aynı rezervasyon için mükerrer arıza bildirimi engellenmelidir")
    void testReportIssue_DuplicateForSameReservation_ShouldThrow() {
        IssueReportDto inputDto = new IssueReportDto();
        inputDto.setChargerId(1L);
        inputDto.setDriverId(1L);
        inputDto.setReservationId(55L);
        inputDto.setDescription("Yine sorun var");

        // Bu rezervasyon için daha önce rapor gönderilmiş
        when(issueReportRepository.existsByRelatedReservationId(55L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> issueReportService.reportIssue(inputDto));

        assertTrue(ex.getMessage().contains("daha önce"),
                "Hata mesajı 'daha önce' içermeli, gerçek: " + ex.getMessage());

        verify(issueReportRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // SENARYO 7D: Var Olmayan Cihaz → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7D - Geçersiz cihaz ID ile arıza bildirimi yapılamamalı")
    void testReportIssue_ChargerNotFound_ShouldThrow() {
        IssueReportDto inputDto = new IssueReportDto();
        inputDto.setChargerId(999L);
        inputDto.setDriverId(1L);
        inputDto.setDescription("Cihaz bulunamadı senaryosu");

        when(chargerRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> issueReportService.reportIssue(inputDto));

        assertTrue(ex.getMessage().contains("bulunamadı"),
                "Hata mesajı 'bulunamadı' içermeli, gerçek: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // SENARYO 7E: Tamir Başladığında Cihaz OFFLINE Yapılmalı
    // Girdi: reportStatus=IN_PROGRESS
    // Beklenen: charger.status → OFFLINE
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7E - Tamir IN_PROGRESS olunca şarj cihazı OFFLINE durumuna geçmeli")
    void testUpdateReportStatus_InProgress_ChargerGoesOffline() {
        savedReport.setTargetCharger(charger);
        when(issueReportRepository.findById(100L)).thenReturn(Optional.of(savedReport));
        when(issueReportRepository.save(any(IssueReport.class))).thenReturn(savedReport);

        issueReportService.updateReportStatus(100L, ReportStatus.IN_PROGRESS);

        // Cihaz OFFLINE yapılmalı
        verify(chargerService, times(1))
                .updateStatus(eq(1L), eq(ChargerStatus.OFFLINE));
    }

    // -----------------------------------------------------------------------
    // SENARYO 7F: Tamir Tamamlandığında Cihaz AVAILABLE Yapılmalı
    // Girdi: reportStatus=RESOLVED
    // Beklenen: charger.status → AVAILABLE
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7F - Tamir RESOLVED olunca şarj cihazı AVAILABLE durumuna geçmeli")
    void testUpdateReportStatus_Resolved_ChargerGoesAvailable() {
        savedReport.setTargetCharger(charger);
        when(issueReportRepository.findById(100L)).thenReturn(Optional.of(savedReport));
        when(issueReportRepository.save(any(IssueReport.class))).thenReturn(savedReport);

        issueReportService.updateReportStatus(100L, ReportStatus.RESOLVED);

        // Cihaz AVAILABLE'a dönmeli
        verify(chargerService, times(1))
                .updateStatus(eq(1L), eq(ChargerStatus.AVAILABLE));
    }

    // -----------------------------------------------------------------------
    // SENARYO 7G: DISMISSED Durumunda Cihaz Durum Değişmemeli
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7G - Rapor DISMISSED olunca cihaz durumu değişmemeli")
    void testUpdateReportStatus_Dismissed_NoChargerChange() {
        savedReport.setTargetCharger(charger);
        when(issueReportRepository.findById(100L)).thenReturn(Optional.of(savedReport));
        when(issueReportRepository.save(any(IssueReport.class))).thenReturn(savedReport);

        issueReportService.updateReportStatus(100L, ReportStatus.DISMISSED);

        // Ne OFFLINE ne de AVAILABLE çağrısı yapılmalı
        verify(chargerService, never()).updateStatus(anyLong(), any());
    }

    // -----------------------------------------------------------------------
    // SENARYO 7H: Operatörsüz İstasyonda Bildirim Atlanmalı (Log Yazılır)
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("7H - Sorumlu operatörü olmayan istasyonda bildirim gönderilmemeli")
    void testReportIssue_NoOperator_NotificationSkipped() {
        // İstasyonun operatörü yok
        station.setResponsibleOperator(null);

        IssueReportDto inputDto = new IssueReportDto();
        inputDto.setChargerId(1L);
        inputDto.setDriverId(1L);
        inputDto.setDescription("Operatörsüz istasyon testi");

        when(chargerRepository.findById(1L)).thenReturn(Optional.of(charger));
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(issueReportRepository.save(any(IssueReport.class))).thenReturn(savedReport);

        // Exception fırlatılmamalı — sistem loglayıp devam eder
        assertDoesNotThrow(() -> issueReportService.reportIssue(inputDto));

        // Bildirim servisine hiç ulaşılmamalı
        verify(notificationService, never()).sendOperatorNotification(anyLong(), anyString(), anyString(), any());
    }
}
