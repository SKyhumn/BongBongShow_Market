package com.example.bongbongshow_market.controller;

import com.example.bongbongshow_market.point.StockChangePoint;
import com.example.bongbongshow_market.service.GoodsStockService;
import com.example.bongbongshow_market.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TestStockController {

    private final StockService stockService;
    private final GoodsStockService goodsStockService;


    @GetMapping("/check") // 주가 변동률을 수동으로 출력
    public ResponseEntity<String> checkStock() {
        // [수정] 서비스에 추가한 전체 데이터 로드 메서드 호출
        stockService.refreshAllTickersData();
        return ResponseEntity.ok("모든 티커의 주가 변동 데이터 수동 로드 완료");
    }

    @PostMapping("/{goodsId}/decrease")
    public ResponseEntity<String> goodsAmountDecrease(
        @PathVariable String goodsId,
        @RequestParam(defaultValue = "1") int amount){
        goodsStockService.applyStockChange(goodsId, amount);
        return ResponseEntity.ok("재고가 감소하였습니다");
    }

    @PostMapping("/{goodsId}/add")
    public ResponseEntity<String> goodsAmountAdd(
        @PathVariable String goodsId,
    @RequestParam(defaultValue = "1") int amount){
        goodsStockService.addStockChange(goodsId, amount);
        return ResponseEntity.ok("재고가 증가하였습니다");
    }
}