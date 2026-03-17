package com.ev.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chargers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "power_output")
    private double powerOutput;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private ChargingStation station;

    @ManyToOne
    @JoinColumn(name = "connector_type_id")
    private ConnecterType connectorType; // [cite: 250]
}
