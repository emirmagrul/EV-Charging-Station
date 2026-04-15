package com.ev.service.impl;

import com.ev.dto.ConnecterTypeDto;
import com.ev.model.ConnecterType;
import com.ev.repository.ConnecterTypeRepository;
import com.ev.service.IConnecterTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnecterTypeServiceImpl implements IConnecterTypeService {

    private final ConnecterTypeRepository connecterTypeRepository;


    @Override
    public ConnecterTypeDto save(ConnecterTypeDto connecterTypeDto) {
        ConnecterType connecterType = new ConnecterType();
        connecterType.setName(connecterTypeDto.getName());

        ConnecterType connecterTypeSaved = connecterTypeRepository.save(connecterType);
        connecterTypeDto.setId(connecterTypeSaved.getId());
        return connecterTypeDto;
    }

    @Override
    public List<ConnecterTypeDto> findAll() {
        return connecterTypeRepository.findAll().stream().map(type -> {
            ConnecterTypeDto dto = new ConnecterTypeDto();
            dto.setId(type.getId());
            dto.setName(type.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ConnecterTypeDto findById(Long id) {
        ConnecterType type = connecterTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soket tipi bulunamadı!"));
        ConnecterTypeDto dto = new ConnecterTypeDto();
        dto.setId(type.getId());
        dto.setName(type.getName());
        return dto;
    }
}
