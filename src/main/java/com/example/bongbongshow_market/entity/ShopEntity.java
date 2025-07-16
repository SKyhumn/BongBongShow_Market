package com.example.bongbongshow_market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "shop")
public class ShopEntity {//엔티티임
    @Id
    private String goods_id;

    private String goods_name;
    private int price;
    private int quantity;

    @Column(name = "updated_price")
    private Double updatedPrice;
}
