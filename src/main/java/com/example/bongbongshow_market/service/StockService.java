package com.example.bongbongshow_market.service;

import com.example.bongbongshow_market.entity.ShopEntity;
import com.example.bongbongshow_market.point.StockChangePoint;
import com.example.bongbongshow_market.repository.StockRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class StockService {
    private final StockRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CachedStock> cache = new HashMap<>();

    @Getter
    private List<StockChangePoint> allIntradayChanges = new ArrayList<>();
    private int currentDataIndex =  0;

    private int dayAgoToFetch = 1;

    @Getter
    private StockChangePoint currentStockPoint = null;

    @PostConstruct
    public void loadInitialStockData(){
        fetchAndProcessAllIntradayData(dayAgoToFetch);
    }

    //@Scheduled(fixedRate = 15 * 60 * 1000) // 15분 마다 재사용하기
    @Scheduled(fixedRate = 10 * 1000) //테스트용
    public void displayNextStockChange(){
        //주간 주식 변동 데이터 리스트가 비어있는지 확인
        if(allIntradayChanges.isEmpty()){
            System.out.println("No intraday stock data available. Attempting to reload...");
            fetchAndProcessAllIntradayData(dayAgoToFetch); // 데이터가 없으면 다시 로드 시도
            if (allIntradayChanges.isEmpty()) {
                System.out.println("Failed to load data.");
                return;
            }
        }
        // 현재 데이터 인덱스(currentDataIndex)가 전체 데이터 리스트의 크기(allIntradayChanges.size())와 같거나 크면
        // (즉, 모든 데이터를 한 바퀴 돌았거나 리스트 범위를 벗어나면)
        // 데이터의 하루전 값으로 데이터를 설정하고
        //인덱스를 0으로 초기화 시킴
        if (currentDataIndex >= allIntradayChanges.size()) {
            System.out.println("현재 날짜의 데이터를 전부 출력 했습니다 하루전 데이터를 설정 합니다");
            dayAgoToFetch++;
            if(dayAgoToFetch > 5){
                System.out.println("현재 날짜가 너무 과거로 설정되어 있습니다 데이터를 현 날짜의 2일전 데이터로 설정합니다");
                dayAgoToFetch = 1;
            }
            fetchAndProcessAllIntradayData(dayAgoToFetch);
            if(allIntradayChanges.isEmpty()){
                System.out.println("데이터를 찾을 수 없습니다 그 전날의 데이터를 설정하겠습니다");
                return;
            }
            currentDataIndex = 0;
        }

        // currentStockPoint가 비워있는데 확인
        if(currentStockPoint == null && !allIntradayChanges.isEmpty()){
            currentStockPoint = allIntradayChanges.get(currentDataIndex);
        } else if (currentStockPoint == null && allIntradayChanges.isEmpty()) {
            System.out.println("current stock point 가 비워 있습니다 더이상 실행하지 않고 종류 하겠습니다");
            return;
        }

        currentStockPoint = allIntradayChanges.get(currentDataIndex);
        double stockChangePercentage = currentStockPoint.getChange();

        // allIntradayChanges 리스트에서 현재 인덱스(currentDataIndex)에 해당하는 StockChangePoint 객체를 가져와
        // currentStockPoint 변수에 저장합니다. 이 값이 AJAX 요청으로 웹에 전달
        System.out.printf("[시간: %s, 기준: %d일 전] 변동률: %.2f%%\n",
                ZonedDateTime.parse(currentStockPoint.getTimestamp()).toLocalTime(), // %s 에 대응
                dayAgoToFetch,                                                  // %d 에 대응
                currentStockPoint.getChange());
        currentDataIndex++;

        List<ShopEntity> allGoods = repository.findAll();
        for (ShopEntity goods : allGoods) {
            double originalPrice = goods.getUpdatedPrice(); // MySQL에서 가져온 상품의 원래 가격

            // 새로운 가격 계산: 원래 가격 * (1 + (변동률 / 100))
            // 예시: 100000 * (1 + (0.5 / 100)) = 100000 * 1.005 = 100500
            double newCalculatedPrice = originalPrice * (1 + (stockChangePercentage / 100.0));

            // 소수점 둘째 자리까지 반올림 (필요한 경우)
            newCalculatedPrice = Math.round(newCalculatedPrice * 100.0) / 100.0;

            System.out.printf("  [상품 ID: %s, 상품명: %s] 원래 가격: %.0f원, 적용 후 가격: %.0f원\n",
                    goods.getGoods_id(), goods.getGoods_name(), originalPrice, newCalculatedPrice);
        }
        repository.saveAll(allGoods);
        currentDataIndex++;
    }


    public void fetchAndProcessAllIntradayData(int dayOffset){
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
             만약 23:30분 부터 6:00분 까지 전부 돌았다면
             하루 전 변동률을 가지고 와서 변동률을 구함
             */
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode result = root.at("/chart/result/0");
            JsonNode timestamps = result.path("timestamp");
            JsonNode closes = result.at("/indicators/quote/0/close");
            double prevClose = result.at("/meta/previousClose").asDouble();

            List<StockChangePoint> tempChanges = new ArrayList<>();

            Double previousConsideredPrice = null;

            ZonedDateTime targetDateStart = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .toLocalDate()
                    .minusDays(dayOffset)
                    .atStartOfDay(ZoneId.of("Asia/Seoul"));
            ZonedDateTime targetDateEnd = targetDateStart.plusDays(1);

            for (int i = 0; i < timestamps.size(); i++) {
                long ts = timestamps.get(i).asLong();
                ZonedDateTime time = Instant.ofEpochSecond(ts).atZone(ZoneId.of("Asia/Seoul"));

                if (closes.get(i) == null || closes.get(i).isNull()) {
                    continue;
                }
                double price = closes.get(i).asDouble();

                if (price == 0.0) {
                    continue;
                }

                ZonedDateTime filterStart = targetDateStart.withHour(23).withMinute(30).withSecond(0).withNano(0);
                ZonedDateTime filterEnd = targetDateEnd.plusDays(1).withHour(6).withMinute(0).withSecond(0).withNano(0);

                if ((time.isEqual(filterStart) || time.isAfter(filterStart)) && time.isBefore(filterEnd)) {
                    double change;

                    if (time.getHour() == 23 && time.getMinute() == 30) {
                        change = ((price - prevClose) / prevClose) * 100;
                        previousConsideredPrice = price; // 23:30 가격을 다음 비교를 위한 기준으로 설정
                    }else if (previousConsideredPrice != null) {
                        change = ((price - previousConsideredPrice) / previousConsideredPrice) * 100;
                        previousConsideredPrice = price; // 현재 가격을 다음 비교를 위한 기준으로 업데이트
                    }
                    else {
                        continue;
                    }
                    change = Math.round(change * 100.0) / 100.0;
                    tempChanges.add(new StockChangePoint(time.toString(), change));
                }
            }

            allIntradayChanges = tempChanges.stream()
                    .sorted(Comparator.comparing(point -> ZonedDateTime.parse(point.getTimestamp()))) // 시간 순으로 정렬
                    .collect(Collectors.toList());

            if (allIntradayChanges.isEmpty()) { // 데이터가 없으면 그 전날 데이터 가지고 오기
                System.out.println("No stock data found within the specified time range (23:30-06:00) for " + ticker);
                dayAgoToFetch++;
                currentStockPoint = null;
            } else {
                System.out.println("Successfully loaded " + allIntradayChanges.size() + " intraday stock points.");
                currentDataIndex = 0;
                currentStockPoint = allIntradayChanges.get(currentDataIndex);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Error fetching or processing stock data: " + e.getMessage());
            allIntradayChanges.clear();
            currentStockPoint = null;
        }
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