package com.example.bongbongshow_market.controller;

import com.example.bongbongshow_market.point.StockChangePoint;
import com.example.bongbongshow_market.service.StockService;
import lombok.RequiredArgsConstructor;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StockApiController {

    @Autowired
    private StockService stockService;

    @GetMapping("/api/current-stock-change")//AJAX로 실시간으로 수정하기
    public Map<String, Object> getCurrentStockChange() {
        StockChangePoint currentPoint = stockService.getCurrentStockPoint();
        Map<String, Object> response = new HashMap<>();

        if (currentPoint != null) {
            response.put("timestamp", ZonedDateTime.parse(currentPoint.getTimestamp()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            response.put("change", String.format("%.2f", currentPoint.getChange())); // 문자열로 포매팅하여 보냄
            response.put("status", "success");
        } else {
            response.put("timestamp", "N/A");
            response.put("change", "N/A");
            response.put("status", "no_data");
        }
        return response;
    }
}
