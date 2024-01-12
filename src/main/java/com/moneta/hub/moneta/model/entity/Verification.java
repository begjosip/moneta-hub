package com.moneta.hub.moneta.model.entity;

import com.moneta.hub.moneta.model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uc_verification__token", columnNames = "token"))
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Verification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private MonetaUser user;
}
