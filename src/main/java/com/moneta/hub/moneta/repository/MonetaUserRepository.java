package com.moneta.hub.moneta.repository;

import com.moneta.hub.moneta.model.entity.MonetaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonetaUserRepository extends JpaRepository<MonetaUser, Long> {

    Optional<MonetaUser> findByUsername(String username);

}
