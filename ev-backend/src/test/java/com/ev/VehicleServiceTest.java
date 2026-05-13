package com.ev;

import com.ev.dto.VehicleDto;
import com.ev.model.ConnectorType;
import com.ev.model.EVDriver;
import com.ev.model.Vehicle;
import com.ev.repository.ConnectorTypeRepository;
import com.ev.repository.EVDriverRepository;
import com.ev.repository.VehicleRepository;
import com.ev.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test Senaryosu 1 — Araç Kayıt & Yönetimi
 * Kapsam: VehicleServiceImpl.registerVehicle(), deleteById(), findByDriverId()
 */
@DisplayName("Test Senaryosu 1: Araç Kayıt ve Yönetimi")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private EVDriverRepository evDriverRepository;

    @Mock
    private ConnectorTypeRepository connectorTypeRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    // ---- Ortak Test Nesneleri ----
    private EVDriver mockDriver;
    private ConnectorType ccsType;
    private Vehicle savedVehicle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockDriver = new EVDriver();
        mockDriver.setId(1L);
        mockDriver.setFirstName("Emir");
        mockDriver.setLastName("Mağrul");

        ccsType = new ConnectorType();
        ccsType.setId(1L);
        ccsType.setName("CCS");

        savedVehicle = new Vehicle();
        savedVehicle.setId(100L);
        savedVehicle.setBrand("Tesla");
        savedVehicle.setModel("Model 3");
        savedVehicle.setBatteryCapacity(75.0);
        savedVehicle.setPlateNumber("35 EV 2024");
        savedVehicle.setOwner(mockDriver);
        savedVehicle.setConnectorType(ccsType);
    }

    // -----------------------------------------------------------------------
    // SENARYO 1A: Başarılı Araç Kaydı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("1A - Yeni araç başarıyla kaydedilmeli")
    void testRegisterVehicle_Success() {
        VehicleDto inputDto = new VehicleDto();
        inputDto.setBrand("Tesla");
        inputDto.setModel("Model 3");
        inputDto.setBatteryCapacity(75.0);
        inputDto.setPlateNumber("35 EV 2024");
        inputDto.setConnectorTypeId(1L);
        inputDto.setDriverId(1L);

        when(vehicleRepository.existsByPlateNumber("35 EV 2024")).thenReturn(false);
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(mockDriver));
        when(connectorTypeRepository.findById(1L)).thenReturn(Optional.of(ccsType));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleDto result = vehicleService.registerVehicle(inputDto);

        // Beklenen: Araç oluşturulur, tüm alanlar girilen değerle eşleşir
        assertNotNull(result);
        assertEquals("Tesla", result.getBrand());
        assertEquals("Model 3", result.getModel());
        assertEquals(75.0, result.getBatteryCapacity());
        assertEquals("35 EV 2024", result.getPlateNumber());
        assertEquals(1L, result.getDriverId());
        assertEquals(1L, result.getConnectorTypeId());

        // Repository'nin save'inin gerçekten çağrıldığını doğrula
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 1B: Mükerrer Plaka Engeli
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("1B - Aynı plaka ile ikinci araç kaydedilememeli")
    void testRegisterVehicle_DuplicatePlate_ShouldThrow() {
        VehicleDto inputDto = new VehicleDto();
        inputDto.setPlateNumber("35 EV 2024");
        inputDto.setDriverId(1L);
        inputDto.setConnectorTypeId(1L);

        // Plaka zaten kayıtlı
        when(vehicleRepository.existsByPlateNumber("35 EV 2024")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vehicleService.registerVehicle(inputDto));

        assertTrue(ex.getMessage().contains("zaten kayıtlı"),
                "Hata mesajı 'zaten kayıtlı' içermeli");

        // save() hiç çağrılmamalı
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    // -----------------------------------------------------------------------
    // SENARYO 1C: Var Olmayan Sürücü ile Araç Kaydı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("1C - Geçersiz sürücü ID'siyle araç kaydı başarısız olmalı")
    void testRegisterVehicle_DriverNotFound_ShouldThrow() {
        VehicleDto inputDto = new VehicleDto();
        inputDto.setPlateNumber("06 TEST 001");
        inputDto.setDriverId(999L);
        inputDto.setConnectorTypeId(1L);

        when(vehicleRepository.existsByPlateNumber("06 TEST 001")).thenReturn(false);
        when(evDriverRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vehicleService.registerVehicle(inputDto));

        assertTrue(ex.getMessage().contains("Sahip Sürücü bulunamadı"),
                "Hata mesajı 'Sahip Sürücü bulunamadı' içermeli");
    }

    // -----------------------------------------------------------------------
    // SENARYO 1D: Var Olmayan Soket Tipi ile Araç Kaydı
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("1D - Geçersiz soket tipi ID'siyle araç kaydı başarısız olmalı")
    void testRegisterVehicle_ConnectorNotFound_ShouldThrow() {
        VehicleDto inputDto = new VehicleDto();
        inputDto.setPlateNumber("35 ABC 999");
        inputDto.setDriverId(1L);
        inputDto.setConnectorTypeId(99L);

        when(vehicleRepository.existsByPlateNumber("35 ABC 999")).thenReturn(false);
        when(evDriverRepository.findById(1L)).thenReturn(Optional.of(mockDriver));
        when(connectorTypeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> vehicleService.registerVehicle(inputDto));

        assertTrue(ex.getMessage().contains("Soket tipi bulunamadı"),
                "Hata mesajı 'Soket tipi bulunamadı' içermeli");
    }

    // -----------------------------------------------------------------------
    // SENARYO 1E: Sürücüye Ait Araçları Listeleme
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("1E - Sürücüye ait araçlar doğru listelenmeli")
    void testFindByDriverId_ReturnsVehicles() {
        Vehicle v1 = new Vehicle();
        v1.setId(1L);
        v1.setBrand("Tesla");
        v1.setModel("Model Y");
        v1.setPlateNumber("35 EV 001");
        v1.setBatteryCapacity(82.0);
        v1.setOwner(mockDriver);
        v1.setConnectorType(ccsType);

        Vehicle v2 = new Vehicle();
        v2.setId(2L);
        v2.setBrand("BMW");
        v2.setModel("iX3");
        v2.setPlateNumber("35 EV 002");
        v2.setBatteryCapacity(74.0);
        v2.setOwner(mockDriver);
        v2.setConnectorType(ccsType);

        when(vehicleRepository.findByOwnerId(1L)).thenReturn(List.of(v1, v2));

        List<VehicleDto> result = vehicleService.findByDriverId(1L);

        assertEquals(2, result.size());
        assertEquals("Tesla", result.get(0).getBrand());
        assertEquals("BMW", result.get(1).getBrand());
    }

    // -----------------------------------------------------------------------
    // SENARYO 1F: Araç Silme
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("1F - Araç başarıyla silinmeli")
    void testDeleteVehicle_Success() {
        doNothing().when(vehicleRepository).deleteById(100L);

        assertDoesNotThrow(() -> vehicleService.deleteById(100L));

        verify(vehicleRepository, times(1)).deleteById(100L);
    }
}
