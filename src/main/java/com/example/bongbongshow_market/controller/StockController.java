package com.example.bongbongshow_market.controller;

import com.example.bongbongshow_market.point.StockChangePoint;
import com.example.bongbongshow_market.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @GetMapping("/check") // 주가 변동률을 수동으로 출력
    public ResponseEntity<String> checkStock() {
        // [수정] 서비스에 추가한 전체 데이터 로드 메서드 호출
        service.refreshAllTickersData();
        return ResponseEntity.ok("모든 티커의 주가 변동 데이터 수동 로드 완료");
    }

    // [수정] URL에 {ticker}를 추가하여 특정 주식 변동률을 요청
    @GetMapping("/change/{ticker}")
    public ResponseEntity<Double> changeStock(@PathVariable String ticker) {
        // [수정] 서비스에서 특정 티커의 현재 포인트를 가져와 변동률을 반환
        StockChangePoint currentPoint = service.getCurrentStockPoint(ticker.toUpperCase());
        if (currentPoint != null) {
            return ResponseEntity.ok(currentPoint.getChange());
        }
        // 데이터가 없을 경우 404 Not Found 응답
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/main") // main 페이지 띄우기
    public String ShowChangePage(Model model) {
        // [수정] 기본 티커(예: "AAPL")의 데이터를 가져와 모델에 추가
        String defaultTicker = "AAPL";
        List<StockChangePoint> change = service.getAllIntradayChanges(defaultTicker);
        System.out.println("HTML로 넘기는 " + defaultTicker + " change = " + change); // html에 change값 넘기기

        // [수정] 모델에 티커 정보와 변동률 리스트를 함께 넘겨주면 더 유용함
        model.addAttribute("ticker", defaultTicker);
        model.addAttribute("changePoints", change);
        model.addAttribute("allTickers", service.getTickers()); // 모든 티커 리스트도 전달

        return "main";
    }
}