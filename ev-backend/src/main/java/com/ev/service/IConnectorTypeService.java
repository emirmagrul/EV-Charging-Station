package com.ev.service;

import com.ev.dto.ConnectorTypeDto;

import java.util.List;

public interface IConnectorTypeService {
    ConnectorTypeDto save(ConnectorTypeDto connectorTypeDto);
    List<ConnectorTypeDto> findAll();
    ConnectorTypeDto findById(Long id);
}
