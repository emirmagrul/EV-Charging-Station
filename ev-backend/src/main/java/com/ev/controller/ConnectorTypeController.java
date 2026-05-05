package com.ev.controller;

import com.ev.dto.ConnectorTypeDto;
import com.ev.service.IConnectorTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connector-types")
@RequiredArgsConstructor
public class ConnectorTypeController {

    private final IConnectorTypeService connectorTypeService;

    @PostMapping
    public ResponseEntity<ConnectorTypeDto> save(@RequestBody ConnectorTypeDto dto) {
        return ResponseEntity.ok(connectorTypeService.save(dto));
    }

    @GetMapping
    public ResponseEntity<List<ConnectorTypeDto>> getAll() {
        return ResponseEntity.ok(connectorTypeService.findAll());
    }
}