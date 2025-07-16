package com.example.bongbongshow_market.controller;

import com.example.bongbongshow_market.point.StockChangePoint;
import com.example.bongbongshow_market.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final StockService service;

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
