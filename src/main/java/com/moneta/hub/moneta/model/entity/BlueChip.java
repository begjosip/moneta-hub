package com.moneta.hub.moneta.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "uc_blue_chip__ticker", columnNames = "ticker"),
                            @UniqueConstraint(name = "uc_blue_chip__company_name", columnNames = "companyName")})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlueChip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticker;

    @Column(nullable = false, unique = true)
    private String companyName;
}
