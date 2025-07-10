package com.example.bongbongshow_market.service;

import com.example.bongbongshow_market.point.StockChangePoint;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
public class StockService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CachedStock> cache = new HashMap<>();

    private List<StockChangePoint> allIntradayChanges = new ArrayList<>();
    private int currentDataIndex =  0;

    @PostConstruct
    public void loadInitialStockData(){
        fetchAndProcessAllIntradayData();
    }

    // @Scheduled(fixedRate = 15 * 60 * 1000) // 15분 마다 재사용하기
    @Scheduled(fixedRate = 10 * 1000) //테스트용
    public void displayNextStockChange(){
        if(allIntradayChanges.isEmpty()){
            System.out.println("No intraday stock data available. Attempting to reload...");
            fetchAndProcessAllIntradayData(); // 데이터가 없으면 다시 로드 시도
            if (allIntradayChanges.isEmpty()) {
                System.out.println("Failed to load data.");
                return;
            }
        }

        if (currentDataIndex >= allIntradayChanges.size()) {
            System.out.println("End of current data sequence. Resetting index or reloading data.");
            currentDataIndex = 0;
        }

        StockChangePoint pointToDisplay = allIntradayChanges.get(currentDataIndex);
        System.out.printf("[시간: %s] 변동률: %.2f%%\n",
                ZonedDateTime.parse(pointToDisplay.getTimestamp()).toLocalTime(),
                pointToDisplay.getChange());
        currentDataIndex++;
    }

    public List<StockChangePoint> getAllIntradayChanges() {
        return allIntradayChanges;
    }

    public List<StockChangePoint> fetchAndProcessAllIntradayData(){
        String ticker = "AAPL";
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker + "?interval=15m&range=5d";

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
             2일전 주가와 3일 전 닫혔을 때 주가를 가지고 온 뒤
             (현재 주가 - 닫힐 때 주가) / 닫힐 때 주가 * 100
             */
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode result = root.at("/chart/result/0");
            JsonNode timestamps = result.path("timestamp");
            JsonNode closes = result.at("/indicators/quote/0/close");
            double prevClose = result.at("/meta/previousClose").asDouble();

            List<StockChangePoint> tempChanges = new ArrayList<>();

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

            for (int i = 0; i < timestamps.size(); i++) {
                long ts = timestamps.get(i).asLong();

                if (closes.get(i) == null || closes.get(i).isNull()) {
                    continue;
                }
                double price = closes.get(i).asDouble();

                if (price == 0.0) {
                    continue;
                }
                ZonedDateTime time = Instant.ofEpochSecond(ts).atZone(ZoneId.of("Asia/Seoul"));
                double change = ((price - prevClose) / prevClose) * 100;
                change = Math.round(change * 100.0) / 100.0;
                tempChanges.add(new StockChangePoint(time.toString(), change));
            }

            ZonedDateTime todayStart = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDate().atStartOfDay(ZoneId.of("Asia/Seoul"));
            ZonedDateTime yesterday2330 = todayStart.minusDays(1).withHour(23).withMinute(30).withSecond(0).withNano(0);
            ZonedDateTime today0600 = todayStart.withHour(6).withMinute(0).withSecond(0).withNano(0);

            allIntradayChanges = tempChanges.stream()
                    .filter(point -> {
                        ZonedDateTime pointTime = ZonedDateTime.parse(point.getTimestamp());
                        return (pointTime.isEqual(yesterday2330) || pointTime.isAfter(yesterday2330)) && pointTime.isBefore(today0600);
                    })
                    .sorted(Comparator.comparing(point -> ZonedDateTime.parse(point.getTimestamp()))) // 시간 순으로 정렬
                    .collect(Collectors.toList());

            if (allIntradayChanges.isEmpty()) {
                System.out.println("No stock data found within the specified time range (23:30-06:00) for " + ticker);
            } else {
                System.out.println("Successfully loaded " + allIntradayChanges.size() + " intraday stock points.");
                currentDataIndex = 0;
            }
            return allIntradayChanges;
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Error fetching or processing stock data: " + e.getMessage());
            allIntradayChanges.clear();
        }
        return Collections.emptyList();
    }

    public double getCachedStockChange(String ticker) {
        if (cache.containsKey(ticker)) {
            return cache.get(ticker).change;
        } else {
            return 0.0;
        }
    }
    static class CachedStock {
        double change;
        long timestamp;

        CachedStock(double change, long timestamp) {
            this.change = change;
            this.timestamp = timestamp;
        }
    }
}