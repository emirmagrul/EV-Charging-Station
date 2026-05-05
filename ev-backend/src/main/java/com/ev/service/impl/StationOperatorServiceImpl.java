package com.ev.service.impl;

import com.ev.dto.StationOperatorDto;
import com.ev.model.StationOperator;
import com.ev.repository.StationOperatorRepository;
import com.ev.service.IStationOperatorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationOperatorServiceImpl implements IStationOperatorService {

    private final StationOperatorRepository operatorRepository;

    @Override
    @Transactional
    public StationOperatorDto save(StationOperatorDto operatorDto) {
        StationOperator operator = new StationOperator();
        operator.setFirstName(operatorDto.getFirstName());
        operator.setLastName(operatorDto.getLastName());
        operator.setEmail(operatorDto.getEmail());

        StationOperator saved = operatorRepository.save(operator);
        operatorDto.setId(saved.getId());
        return operatorDto;
    }

    @Override
    public List<StationOperatorDto> findAll() {
        return operatorRepository.findAll().stream().map(op -> {
            StationOperatorDto dto = new StationOperatorDto();
            dto.setId(op.getId());
            dto.setFirstName(op.getFirstName());
            dto.setLastName(op.getLastName());
            dto.setEmail(op.getEmail());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public StationOperatorDto findById(Long id) {
        StationOperator op = operatorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Operatör bulunamadı!"));
        StationOperatorDto dto = new StationOperatorDto();
        dto.setId(op.getId());
        dto.setFirstName(op.getFirstName());
        dto.setLastName(op.getLastName());
        dto.setEmail(op.getEmail());
        return dto;
    }
}
