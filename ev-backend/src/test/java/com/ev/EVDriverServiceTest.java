package com.ev;

import com.ev.dto.EVDriverDto;
import com.ev.model.EVDriver;
import com.ev.model.enums.UserRole;
import com.ev.repository.AdminRepository;
import com.ev.repository.ChargingStationRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.StationOperatorRepository;
import com.ev.service.INotificationService;
import com.ev.service.impl.EVDriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test Senaryosu 4 — Sürücü Hesap İşlemleri
 * Kapsam: createDriver(), login(), deductBalance(), addBalance()
 */
@DisplayName("Test Senaryosu 4: Sürücü Hesap İşlemleri")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class EVDriverServiceTest {

    @Mock private EVDriverRepository evDriverRepository;
    @Mock private ChargingStationRepository chargingStationRepository;
    @Mock private StationOperatorRepository operatorRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private INotificationService notificationService;

    @InjectMocks
    private EVDriverServiceImpl evDriverService;

    private EVDriver existingDriver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingDriver = new EVDriver();
        existingDriver.setId(1L);
        existingDriver.setFirstName("Emir");
        existingDriver.setLastName("Mağrul");
        existingDriver.setEmail("emir@ev.com");
        existingDriver.setPassword("pass123");
        existingDriver.setWalletBalance(new BigDecimal("500.00"));
        existingDriver.setRole(UserRole.DRIVER);
    }

    // -----------------------------------------------------------------------
    // SENARYO 4A: Başarılı Sürücü Kaydı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4A - Yeni sürücü başarıyla kayıt olabilmeli")
    void testCreateDriver_Success() {
        EVDriverDto inputDto = new EVDriverDto();
        inputDto.setFirstName("Ali");
        inputDto.setLastName("Yılmaz");
        inputDto.setEmail("ali@ev.com");
        inputDto.setPassword("sifre123");
        inputDto.setRole(UserRole.DRIVER);

        when(evDriverRepository.findByEmail("ali@ev.com")).thenReturn(Optional.empty());

        EVDriver savedDriver = new EVDriver();
        savedDriver.setId(2L);
        savedDriver.setFirstName("Ali");
        savedDriver.setLastName("Yılmaz");
        savedDriver.setEmail("ali@ev.com");
        savedDriver.setRole(UserRole.DRIVER);
        savedDriver.setWalletBalance(BigDecimal.ZERO);

        when(evDriverRepository.save(any(EVDriver.class))).thenReturn(savedDriver);

        EVDriverDto result = evDriverService.createDriver(inputDto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertNull(result.getPassword(), "Şifre response'a dönmemeli");
        assertEquals(UserRole.DRIVER, result.getRole());
        verify(evDriverRepository, times(1)).save(any(EVDriver.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 4B: Mükerrer E-posta ile Kayıt → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4B - Kayıtlı e-posta adresiyle yeni kayıt yapılamamalı")
    void testCreateDriver_DuplicateEmail_ShouldThrow() {
        EVDriverDto inputDto = new EVDriverDto();
        inputDto.setEmail("emir@ev.com");

        when(evDriverRepository.findByEmail("emir@ev.com")).thenReturn(Optional.of(existingDriver));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evDriverService.createDriver(inputDto));

        assertTrue(ex.getMessage().contains("zaten kullanımda"),
                "Hata mesajı 'zaten kullanımda' içermeli");

        verify(evDriverRepository, never()).save(any(EVDriver.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 4C: OPERATOR/ADMIN rolü bu servis üzerinden kaydedilemez
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4C - OPERATOR rolüyle kayıt bu servis üzerinden yapılamamalı")
    void testCreateDriver_OperatorRole_ShouldThrow() {
        EVDriverDto inputDto = new EVDriverDto();
        inputDto.setEmail("operator@ev.com");
        inputDto.setRole(UserRole.OPERATOR);

        when(evDriverRepository.findByEmail("operator@ev.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evDriverService.createDriver(inputDto));

        assertTrue(ex.getMessage().contains("yanlış servis"),
                "Hata mesajı 'yanlış servis' içermeli");
    }

    // -----------------------------------------------------------------------
    // SENARYO 4D: Başarılı Giriş
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4D - Doğru email ve şifre ile giriş başarılı olmalı")
    void testLogin_Success() {
        when(evDriverRepository.findByEmail("emir@ev.com")).thenReturn(Optional.of(existingDriver));

        EVDriverDto result = evDriverService.login("emir@ev.com", "pass123", UserRole.DRIVER);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Emir", result.getFirstName());
    }

    // -----------------------------------------------------------------------
    // SENARYO 4E: Yanlış Şifre ile Giriş → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4E - Yanlış şifre ile giriş reddedilmeli")
    void testLogin_WrongPassword_ShouldThrow() {
        when(evDriverRepository.findByEmail("emir@ev.com")).thenReturn(Optional.of(existingDriver));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evDriverService.login("emir@ev.com", "yanlis_sifre", UserRole.DRIVER));

        assertTrue(ex.getMessage().contains("hatalı"),
                "Hata mesajı 'hatalı' içermeli");
    }

    // -----------------------------------------------------------------------
    // SENARYO 4F: Bakiye Düşme — Başarılı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4F - Yeterli bakiye varsa para başarıyla düşülmeli")
    void testDeductBalance_Success() {
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(evDriverRepository.save(any(EVDriver.class))).thenReturn(existingDriver);

        // Hiç exception fırlatılmamalı
        assertDoesNotThrow(() -> evDriverService.deductBalance(1L, new BigDecimal("200.00")));

        // 500 - 200 = 300
        assertEquals(0, existingDriver.getWalletBalance().compareTo(new BigDecimal("300.00")));
        verify(evDriverRepository, times(1)).save(existingDriver);
    }

    // -----------------------------------------------------------------------
    // SENARYO 4G: Yetersiz Bakiye → Exception
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4G - Yetersiz bakiye durumunda ödeme reddedilmeli")
    void testDeductBalance_InsufficientFunds_ShouldThrow() {
        existingDriver.setWalletBalance(new BigDecimal("50.00"));
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> evDriverService.deductBalance(1L, new BigDecimal("200.00")));

        assertTrue(ex.getMessage().contains("Yetersiz bakiye"),
                "Hata mesajı 'Yetersiz bakiye' içermeli");

        // Bakiye değişmemeli
        assertEquals(0, existingDriver.getWalletBalance().compareTo(new BigDecimal("50.00")));
        verify(evDriverRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // SENARYO 4H: Bakiye Yükleme — Başarılı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4H - Bakiye başarıyla yüklenebilmeli")
    void testAddBalance_Success() {
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(evDriverRepository.save(any(EVDriver.class))).thenReturn(existingDriver);

        assertDoesNotThrow(() -> evDriverService.addBalance(1L, new BigDecimal("250.00")));

        // 500 + 250 = 750
        assertEquals(0, existingDriver.getWalletBalance().compareTo(new BigDecimal("750.00")));
    }

    // -----------------------------------------------------------------------
    // SENARYO 4I: Düşük Bakiye Bildirimi — 100 TL Altı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4I - Bakiye 100 TL'nin altına düştüğünde bildirim gönderilmeli")
    void testDeductBalance_LowBalanceNotification() {
        existingDriver.setWalletBalance(new BigDecimal("150.00"));
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(evDriverRepository.save(any(EVDriver.class))).thenReturn(existingDriver);

        // 150 - 80 = 70 → 100'ün altında, bildirim gitmeli
        evDriverService.deductBalance(1L, new BigDecimal("80.00"));

        verify(notificationService, times(1)).sendNotification(
                eq(1L), anyString(), anyString(),
                eq(com.ev.model.enums.NotificationType.WALLET_ALERT)
        );
    }

    // -----------------------------------------------------------------------
    // SENARYO 4J: Düşük Bakiye Bildirimi — 100 TL Üstü (Bildirim Gitme!)
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("4J - Bakiye 100 TL'nin üzerinde kalırsa bildirim gönderilmemeli")
    void testDeductBalance_NoNotificationAboveThreshold() {
        existingDriver.setWalletBalance(new BigDecimal("500.00"));
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(existingDriver));
        when(evDriverRepository.save(any(EVDriver.class))).thenReturn(existingDriver);

        // 500 - 100 = 400 → hala 100 üzeri, bildirim gitmemeli
        evDriverService.deductBalance(1L, new BigDecimal("100.00"));

        verify(notificationService, never()).sendNotification(anyLong(), anyString(), anyString(), any());
    }
}
