package com.example.bongbongshow_market.controller;

import com.example.bongbongshow_market.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @GetMapping("/check") //주가 변동률을 수동으로 출력
    public ResponseEntity<String> checkStock(){
        service.fetchStockChange();
        return ResponseEntity.ok("주가 변동 완료");
    }
}
