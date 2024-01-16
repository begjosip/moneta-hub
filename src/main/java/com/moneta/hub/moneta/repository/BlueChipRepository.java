package com.moneta.hub.moneta.repository;

import com.moneta.hub.moneta.model.entity.BlueChip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlueChipRepository extends JpaRepository<BlueChip, Long> {


}
