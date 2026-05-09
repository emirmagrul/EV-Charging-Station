package com.ev.dto;

import lombok.Data;

@Data
public class UserActivityDto {
    private long totalDrivers;
    private long activeReservations; // PENDING ve CONFIRMED olanlar
    private long completedSessionsToday;
}
