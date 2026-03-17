package com.ev.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EVDriverDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal walletBalance;
}
