package com.example.bongbongshow_market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ShopEntity {//엔티티임
    @Id
    private String goods_id;

    private String goods_name;
    private int price;
    private int quantity;
}
