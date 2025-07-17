package com.example.bongbongshow_market.service;

import com.example.bongbongshow_market.entity.ShopEntity;
import com.example.bongbongshow_market.exception.InsufficientStockException;
import com.example.bongbongshow_market.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoodsStockService {
    private final StockRepository repository;

    @Transactional
    public void applyStockChange(String goodId, int quantity){
        ShopEntity shopEntity = repository.findById(goodId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않음"));
        int current = shopEntity.getQuantity();
        if(current < quantity){
            throw new InsufficientStockException("재고가 부족합니다");
        }

        shopEntity.setQuantity(current - quantity);
        repository.save(shopEntity);
    }

    @Transactional
    public void addStockChange(String goodId, int quantity){
        ShopEntity shopEntity = repository.findById(goodId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않음"));
        int current = shopEntity.getQuantity();
        int updateQuantity = current + quantity;

        if(updateQuantity < 0){
            throw new IllegalArgumentException("재고량이 음수가 될 수 없다");
        }

        shopEntity.setQuantity(updateQuantity);
        repository.save(shopEntity);
    }

}
