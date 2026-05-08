package com.ev.dto;

import lombok.Data;

@Data
public class StationOperatorDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private com.ev.model.enums.UserRole role;
}
