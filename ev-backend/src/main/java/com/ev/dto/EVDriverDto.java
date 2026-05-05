package com.ev.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EVDriverDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal walletBalance;
    private List<Long> favoriteStationIds;
}
