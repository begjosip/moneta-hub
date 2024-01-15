package com.moneta.hub.moneta.repository;

import com.moneta.hub.moneta.model.entity.Verification;
import com.moneta.hub.moneta.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {

    Optional<Verification> findByTokenAndStatus(String token, VerificationStatus status);

    List<Verification> findAllByUserIdAndStatus(Long userId, VerificationStatus verificationStatus);
}
