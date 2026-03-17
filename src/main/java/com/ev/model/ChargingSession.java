package com.ev.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "charging_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "energy_consumed_kwh")
    private double energyConsumedKwh;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @OneToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}
