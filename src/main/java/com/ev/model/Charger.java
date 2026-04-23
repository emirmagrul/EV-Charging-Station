package com.ev.model;

import com.ev.model.enums.ChargerStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ChargerStatus status;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private ChargingStation station;

    @ManyToOne
    @JoinColumn(name = "connector_type_id")
    private ConnectorType connectorType; // [cite: 250]
}
