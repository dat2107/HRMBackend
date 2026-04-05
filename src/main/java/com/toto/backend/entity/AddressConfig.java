package com.toto.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "province", nullable = false, length = 100)
    private String province;

    @Column(name = "district", nullable = false, length = 100)
    private String district;
}
