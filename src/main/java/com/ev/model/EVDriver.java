package com.ev.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "ev_drivers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EVDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "wallet_balance")
    private BigDecimal walletBalance;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Vehicle> vehicles;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "driver_favorite_stations",
            joinColumns = @JoinColumn(name = "driver_id"),
            inverseJoinColumns = @JoinColumn(name = "station_id")
    )
    private List<ChargingStation> favoriteStations;
}
