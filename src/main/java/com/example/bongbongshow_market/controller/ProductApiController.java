package com.example.bongbongshow_market.controller;

import com.example.bongbongshow_market.entity.ShopEntity;
import com.example.bongbongshow_market.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ProductApiController {
    private final StockRepository repository;

    @GetMapping("/api/products/realtime")
    public List<ProductDto> getRealtimeProducts() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(ShopEntity::getGoods_id))
                .map(ProductDto::new)
                .collect(Collectors.toList());
    }

    record ProductDto(String goods_id, String goods_name, int price, double updatedPrice) {
        public ProductDto(ShopEntity entity) {
            this(entity.getGoods_id(), entity.getGoods_name(), entity.getPrice(),
                    entity.getUpdatedPrice() != null ? entity.getUpdatedPrice() : 0.0);
        }
    }
}
