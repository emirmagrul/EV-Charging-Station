package com.ev.service;

import com.ev.dto.ConnecterTypeDto;

import java.util.List;

public interface IConnecterTypeService {
    ConnecterTypeDto save(ConnecterTypeDto connecterTypeDto);
    List<ConnecterTypeDto> findAll();
    ConnecterTypeDto findById(Long id);
}
