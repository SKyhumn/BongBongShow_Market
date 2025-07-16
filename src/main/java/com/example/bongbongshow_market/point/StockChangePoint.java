package com.example.bongbongshow_market.point;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockChangePoint {
    public String timestamp;
    public double change;

    public StockChangePoint(String timestamp, double change) {
        this.timestamp = timestamp;
        this.change = change;
    }
    @Override
    public String toString() {
        return "시간: " + timestamp + ", 변동률: " + change + "%";
    }
}
