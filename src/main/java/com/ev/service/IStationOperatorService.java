package com.ev.service;

import com.ev.dto.StationOperatorDto;

import java.util.List;

public interface IStationOperatorService {
    StationOperatorDto save(StationOperatorDto operatorDto);
    List<StationOperatorDto> findAll();
    StationOperatorDto findById(Long id);
}
