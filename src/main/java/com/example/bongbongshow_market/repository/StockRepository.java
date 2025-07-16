package com.example.bongbongshow_market.repository;

import com.example.bongbongshow_market.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<ShopEntity, String> {
}
