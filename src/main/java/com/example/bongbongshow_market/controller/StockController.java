package com.example.bongbongshow_market.controller;

import com.example.bongbongshow_market.point.StockChangePoint;
import com.example.bongbongshow_market.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @GetMapping("/check") //주가 변동률을 수동으로 출력
    public ResponseEntity<String> checkStock(){
        service.fetchStockChange();
        return ResponseEntity.ok("주가 변동 완료");
    }

    //주가 변동률을 JSON 숫자 형태로 반환
    @GetMapping("/change")
    public ResponseEntity<Double> changeStock(){
        double change = service.getCachedStockChange("AAPL");
        return ResponseEntity.ok(change);
    }

    @GetMapping("/main")// main 페이지 띄우기
    public String ShowChangePage(Model model) {
        List<StockChangePoint> change = service.fetchStockChange();
        System.out.println("HTML로 넘기는 change = " + change); //html에 change값 넘기기
        model.addAttribute("change", change);
        return "main";
    }
}
