package com.ev.service.impl;

import com.ev.dto.ConnectorTypeDto;
import com.ev.model.ConnectorType;
import com.ev.repository.ConnectorTypeRepository;
import com.ev.service.IConnectorTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConnectorTypeServiceImpl implements IConnectorTypeService {

    private final ConnectorTypeRepository connectorTypeRepository;


    @Override
    public ConnectorTypeDto save(ConnectorTypeDto connectorTypeDto) {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName(connectorTypeDto.getName());

        ConnectorType connectorTypeSaved = connectorTypeRepository.save(connectorType);
        connectorTypeDto.setId(connectorTypeSaved.getId());
        return connectorTypeDto;
    }

    @Override
    public List<ConnectorTypeDto> findAll() {
        return connectorTypeRepository.findAll().stream().map(type -> {
            ConnectorTypeDto dto = new ConnectorTypeDto();
            dto.setId(type.getId());
            dto.setName(type.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ConnectorTypeDto findById(Long id) {
        ConnectorType type = connectorTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soket tipi bulunamadı!"));
        ConnectorTypeDto dto = new ConnectorTypeDto();
        dto.setId(type.getId());
        dto.setName(type.getName());
        return dto;
    }
}
