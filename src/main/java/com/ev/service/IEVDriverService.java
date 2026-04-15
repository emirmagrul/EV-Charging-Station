package com.ev.service;

import com.ev.dto.EVDriverDto;
import com.ev.model.EVDriver;

import java.math.BigDecimal;

public interface IEVDriverService {
    EVDriverDto createDriver(EVDriverDto evDriverDto);
    void addBalance(Long driverId, BigDecimal amount);
    void deductBalance(Long driverId, BigDecimal amount);
    EVDriverDto findById(Long id);
}
