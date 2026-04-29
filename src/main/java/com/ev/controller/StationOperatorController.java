package com.ev.controller;

import com.ev.dto.StationOperatorDto;
import com.ev.service.IStationOperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operators")
@RequiredArgsConstructor
public class StationOperatorController {

    private final IStationOperatorService operatorService;

    @PostMapping
    public ResponseEntity<StationOperatorDto> save(@RequestBody StationOperatorDto dto) {
        return ResponseEntity.ok(operatorService.save(dto));
    }

    @GetMapping
    public ResponseEntity<List<StationOperatorDto>> getAll() {
        return ResponseEntity.ok(operatorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StationOperatorDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(operatorService.findById(id));
    }
}