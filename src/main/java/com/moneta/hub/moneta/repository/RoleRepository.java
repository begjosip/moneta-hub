package com.moneta.hub.moneta.repository;

import com.moneta.hub.moneta.model.entity.Role;
import com.moneta.hub.moneta.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    List<Role> findAllByNameIn(List<UserRole> roles);
}
