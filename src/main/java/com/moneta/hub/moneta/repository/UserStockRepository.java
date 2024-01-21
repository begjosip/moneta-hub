package com.moneta.hub.moneta.repository;

import com.moneta.hub.moneta.model.entity.UserStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStockRepository extends JpaRepository<UserStock, Long> {

    Optional<UserStock> findByTickerAndUserId(String ticker, Long userId);

    List<UserStock> findAllByUserId(Long userId);
}
