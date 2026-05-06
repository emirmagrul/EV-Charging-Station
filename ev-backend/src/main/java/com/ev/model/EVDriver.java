package com.ev.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;


    @Column(name = "wallet_balance")
    private BigDecimal walletBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private com.ev.model.enums.UserRole role = com.ev.model.enums.UserRole.DRIVER;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Vehicle> vehicles;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "driver_favorite_stations", joinColumns = @JoinColumn(name = "driver_id"), inverseJoinColumns = @JoinColumn(name = "station_id"))
    private Set<ChargingStation> favoriteStations;
}
