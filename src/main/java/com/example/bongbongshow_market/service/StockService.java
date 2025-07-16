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
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {
    private final StockRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, List<StockChangePoint>> allIntradayChangesMap = new HashMap<>();
    private final Map<String, Integer> currentDataIndexMap = new HashMap<>();
    private final Map<String, StockChangePoint> currentStockPointMap = new HashMap<>();



    private final Map<String, Integer> tickerDayAgoToFetchMap = new HashMap<>();

    private final List<String> tickers = Arrays.asList("AAPL", "MSFT", "GOOG", "AMZN", "NVDA", "TSLA", "META", "NFLX");

    public StockChangePoint getCurrentStockPoint(String ticker) {
        return currentStockPointMap.get(ticker);
    }

    public List<StockChangePoint> getAllIntradayChanges(String ticker) {
        return allIntradayChangesMap.getOrDefault(ticker, new ArrayList<>());
    }

    public List<String> getTickers() {
        return Collections.unmodifiableList(tickers);
    }

    public void refreshAllTickersData() {
        for (String ticker : tickers) {
            fetchAndProcessAllIntradayData(ticker, 1);
            tickerDayAgoToFetchMap.put(ticker, 1);
            currentDataIndexMap.put(ticker, 0);
        }
    }

    @PostConstruct
    public void loadInitialStockData(){
        for (String ticker : tickers) {
            tickerDayAgoToFetchMap.put(ticker, 1);
            fetchAndProcessAllIntradayData(ticker, tickerDayAgoToFetchMap.get(ticker));
        }
    }

    //@Scheduled(fixedRate = 15 * 60 * 1000) // 15분 마다 재사용하기
    @Scheduled(fixedRate = 10 * 1000) //테스트용
    public void displayNextStockChange(){
        for (String ticker : tickers){
            List<StockChangePoint> currentTickerChanges = allIntradayChangesMap.getOrDefault(ticker, new ArrayList<>());
            int currentTickerDataIndex = currentDataIndexMap.getOrDefault(ticker, 0);
            int currentDayOffset = tickerDayAgoToFetchMap.getOrDefault(ticker, 1); // 해당 티커의 dayOffset 가져옴

            //주간 주식 변동 데이터 리스트가 비어있는지 확인
            if(currentTickerChanges.isEmpty()){
                System.out.println("No intraday stock data available for " + ticker + ". Attempting to load next day's data...");
                currentDayOffset++; // ⭐ 데이터가 없으니 즉시 다음 날짜로 넘김
                if(currentDayOffset > 16){
                    currentDayOffset = 1;
                }
                tickerDayAgoToFetchMap.put(ticker, currentDayOffset);
                fetchAndProcessAllIntradayData(ticker, currentDayOffset);
                currentTickerChanges = allIntradayChangesMap.get(ticker);
                if (currentTickerChanges == null || currentTickerChanges.isEmpty()) {
                    System.out.println("Failed to load any data for " + ticker + " even after attempting reload. Skipping.");
                    continue;
                }
                currentTickerDataIndex = 0;
                currentDataIndexMap.put(ticker, currentTickerDataIndex);
            }
            // 현재 데이터 인덱스(currentDataIndex)가 전체 데이터 리스트의 크기(allIntradayChanges.size())와 같거나 크면
            // (즉, 모든 데이터를 한 바퀴 돌았거나 리스트 범위를 벗어나면)
            // 데이터의 하루전 값으로 데이터를 설정하고
            //인덱스를 0으로 초기화 시킴
            if (currentTickerDataIndex >= currentTickerChanges.size()) {
                System.out.println("All data points for " + ticker + " on current dayOffset " + currentDayOffset + " processed. Moving to next day's data.");
                currentDayOffset++;
                if(currentDayOffset > 15){
                    currentDayOffset = 1;
                }
                tickerDayAgoToFetchMap.put(ticker, currentDayOffset);
                fetchAndProcessAllIntradayData(ticker, currentDayOffset);
                currentTickerChanges = allIntradayChangesMap.get(ticker);
                if(currentTickerChanges == null || currentTickerChanges.isEmpty()){
                    System.out.println(ticker + "에 대해 데이터를 찾을 수 없습니다. 다음 날짜로 다시 시도하겠습니다.");
                    continue;
                }
                currentTickerDataIndex = 0;
                currentDataIndexMap.put(ticker, currentTickerDataIndex);
            }

            // currentTickerStockPoint 업데이트
            StockChangePoint currentTickerStockPoint = currentTickerChanges.get(currentTickerDataIndex);
            double stockChangePercentage = currentTickerStockPoint.getChange() * 10;

            // allIntradayChanges 리스트에서 현재 인덱스(currentDataIndex)에 해당하는 StockChangePoint 객체를 가져와
            // currentStockPoint 변수에 저장합니다. 이 값이 AJAX 요청으로 웹에 전달
            System.out.printf("[티커: %s, 시간: %s, 기준: %d일 전] 변동률: %.2f%%\n",
                    ticker,
                    ZonedDateTime.parse(currentTickerStockPoint.getTimestamp()).toLocalTime(),
                    currentDayOffset, // 현재 티커의 currentDayOffset 사용
                    stockChangePercentage);

            Map<String, String> goodsToTickerMap = new HashMap<>();
            goodsToTickerMap.put("g01", "AAPL");
            goodsToTickerMap.put("g02", "MSFT");
            goodsToTickerMap.put("g03", "GOOG");
            goodsToTickerMap.put("g04", "AMZN");
            goodsToTickerMap.put("g05", "NVDA");
            goodsToTickerMap.put("g06", "TSLA");
            goodsToTickerMap.put("g07", "META");
            goodsToTickerMap.put("g08", "NFLX");

            List<ShopEntity> allGoods = repository.findAll(); // 모든 상품을 가져옴

            for (ShopEntity goods : allGoods) {
                String targetTicker = goodsToTickerMap.get(goods.getGoods_id());
                if (!ticker.equals(targetTicker)) continue;
                // 특정 티커와 상품을 매핑하는 로직이 없으므로 현재는 모든 상품에 이 변동률을 적용함
                double originalPrice = goods.getUpdatedPrice();
                double newCalculatedPrice = originalPrice * (1 + (stockChangePercentage / 100.0));
                newCalculatedPrice = Math.round(newCalculatedPrice * 100.0) / 100.0;

                goods.setUpdatedPrice(newCalculatedPrice); // ⭐ 업데이트된 가격 설정
                System.out.printf("  [상품 ID: %s, 상품명: %s] 원래 가격: %.0f원, 적용 후 가격: %.0f원\n",
                        goods.getGoods_id(), goods.getGoods_name(), originalPrice, newCalculatedPrice);
            }
            repository.saveAll(allGoods); // 변경된 모든 상품 저장
            currentTickerDataIndex++;
            currentDataIndexMap.put(ticker, currentTickerDataIndex); // 인덱스 업데이트
            currentStockPointMap.put(ticker, currentTickerStockPoint); // 현재 포인트 업데이트 (다음 스케줄링 시 사용)
        }
    }


    public void fetchAndProcessAllIntradayData(String ticker ,int dayOffset){
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker + "?interval=15m&range=15d";

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

            ZonedDateTime targetDateStartET = ZonedDateTime.now(ZoneId.of("America/New_York"))
                    .toLocalDate()
                    .minusDays(dayOffset)
                    .atStartOfDay(ZoneId.of("America/New_York"));
            ZonedDateTime targetDateEndET = targetDateStartET.plusDays(1);

            System.out.println("--- Fetching data for " + ticker + " (dayOffset: " + dayOffset + ") ---");
            System.out.println("Target Date Start (ET): " + targetDateStartET);
            System.out.println("Target Date End (ET): " + targetDateEndET);


            for (int i = 0; i < timestamps.size(); i++) {
                long ts = timestamps.get(i).asLong();
                ZonedDateTime timeET = Instant.ofEpochSecond(ts).atZone(ZoneId.of("America/New_York"));

                if (closes.get(i) == null || closes.get(i).isNull()) {
                    continue;
                }
                double price = closes.get(i).asDouble();

                if (price == 0.0) {
                    continue;
                }

                boolean isMarketOpenHours = (timeET.getHour() > 9 || (timeET.getHour() == 9 && timeET.getMinute() >= 30))
                        && (timeET.getHour() < 16);

                boolean isWeekday = timeET.getDayOfWeek() != DayOfWeek.SATURDAY && timeET.getDayOfWeek() != DayOfWeek.SUNDAY;


                if (timeET.toLocalDate().isEqual(targetDateStartET.toLocalDate()) && isMarketOpenHours && isWeekday) {
                    double change;

                    if (timeET.getHour() == 9 && timeET.getMinute() == 30) {
                        change = ((price - prevClose) / prevClose) * 100;
                        previousConsideredPrice = price;
                    } else if (previousConsideredPrice != null) {
                        change = ((price - previousConsideredPrice) / previousConsideredPrice) * 100;
                        previousConsideredPrice = price;
                    } else {
                        continue;
                    }
                    change = Math.round(change * 100.0) / 100.0;
                    tempChanges.add(new StockChangePoint(timeET.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toString(), change));
                }
            }

            List<StockChangePoint> sortedChanges = tempChanges.stream()
                    .sorted(Comparator.comparing(point -> ZonedDateTime.parse(point.getTimestamp())))
                    .collect(Collectors.toList());

            allIntradayChangesMap.put(ticker, sortedChanges);
            currentDataIndexMap.put(ticker, 0);
            if (!sortedChanges.isEmpty()) {
                currentStockPointMap.put(ticker, sortedChanges.get(0));
                System.out.println("Successfully loaded " + sortedChanges.size() + " intraday stock points for " + ticker + ".");
            } else {
                currentStockPointMap.put(ticker, null);
                // ⭐ 핵심 수정: 데이터를 찾지 못하면 여기에 로그를 남기고,
                // 이 상황을 displayNextStockChange 메서드에서 처리하도록 유도합니다.
                System.out.println("No market open data found for " + ticker + " on target date (" + targetDateStartET.toLocalDate() + ").");
            }


        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Error fetching or processing stock data for " + ticker + ": " + e.getMessage());
            allIntradayChangesMap.put(ticker, new ArrayList<>());
            currentDataIndexMap.put(ticker, 0);
            currentStockPointMap.put(ticker, null);
        }
    }

    /*
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
    */
}