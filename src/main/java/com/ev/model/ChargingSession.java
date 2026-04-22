package com.ev.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "energy_consumed_kwh")
    private double energyConsumedKwh;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "status") // ACTIVE, INACTIVE gibi durumlar için
    private String status;

    @OneToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}
