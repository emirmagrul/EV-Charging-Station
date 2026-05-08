package com.ev.model;

import com.ev.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "station_operator")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationOperator {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role = UserRole.OPERATOR;

    @OneToMany(mappedBy = "responsibleOperator")
    @ToString.Exclude
    private List<ChargingStation> responsibleStations;
}
