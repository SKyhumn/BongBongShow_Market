package com.example.bongbongshow_market.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CachedStock> cache = new HashMap<>();

    @Scheduled(fixedRate = 15 * 60 * 1000)
    public double fetchStockChange(){
        String ticker = "AAPL";
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker + "?interval=15m&range=1d";
       if(cache.containsKey(ticker)){  //요청 간격 15분으로 제어
           CachedStock cachedStock = cache.get(ticker);
           if(System.currentTimeMillis() - cachedStock.timestamp < 15 * 60 * 1000){
               System.out.printf("캐시 사용 - %s 주가 변동률: %.2f%%\n", ticker, cachedStock.change);
               return cachedStock.change;
           }
       }

        try{ // 주가 변동 퍼센트로 가지고 오기
            HttpHeaders headers = new HttpHeaders(); // 브라우저 처럼 보이게 하기
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response  = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            /*
             주가 변동 데이터 가지고 오기
             현재 주가와 닫혔을 때 주가를 기지고 온 뒤
             (현재 주가 - 닫힐 때 주가) / 닫힐 때 주가 * 100
             */
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode result = root.at("/chart/result/0");
            double current = result.at("/meta/regularMarketPrice").asDouble();
            double prevClose = result.at("/meta/previousClose").asDouble();

            double change = ((current - prevClose) / prevClose) * 100;
            change = Math.round(change * 100.0) / 100.0;
            cache.put(ticker, new CachedStock(change, System.currentTimeMillis()));

            System.out.printf("API 호출 - %s 주가 변동률: %.2f%%\n", ticker, change);

            return change;
        }catch (Exception e){ // 오류 났을때 오류 코드 출력
            e.printStackTrace();
        }
        return 0;
    }

    public double getCachedStockChange(String ticker){
        if(cache.containsKey(ticker)){
            return cache.get(ticker).change;
        }else {
            return 0.0;
        }
    }
    static class CachedStock {// 주식 변동을 캐싱하기 위해
        double change;
        long timestamp;
        CachedStock(double change, long timestamp){
            this.change = change;
            this.timestamp = timestamp;
        }
    }
}
