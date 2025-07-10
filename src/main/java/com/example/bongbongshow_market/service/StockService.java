    package com.example.bongbongshow_market.service;

    import com.example.bongbongshow_market.point.StockChangePoint;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Service
    public class StockService {
        private final RestTemplate restTemplate = new RestTemplate();
        private final Map<String, CachedStock> cache = new HashMap<>();

        @Scheduled(fixedRate = 15 * 60 * 1000)
        public List<StockChangePoint> fetchStockChange(){
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
                 2일전 주가와 3일 전 닫혔을 때 주가를 기지고 온 뒤
                 (현재 주가 - 닫힐 때 주가) / 닫힐 때 주가 * 100
                 */
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                JsonNode result = root.at("/chart/result/0");
                JsonNode timestamps = result.path("timestamp");
                JsonNode closes = result.at("/indicators/quote/0/close");
                double prevClose = result.at("/meta/previousClose").asDouble();

                List<StockChangePoint> changes = new ArrayList<>();
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                ZonedDateTime twoDaysAgo = now.minusDays(2);

                ZonedDateTime start = twoDaysAgo.withHour(23).withMinute(30).withSecond(0).withNano(0);
                ZonedDateTime end = twoDaysAgo.plusDays(1).withHour(6).withMinute(0).withSecond(0).withNano(0);

                for (int i = 0; i < timestamps.size(); i++) {
                    long ts = timestamps.get(i).asLong();
                    double price = closes.get(i).asDouble();

                    ZonedDateTime time = Instant.ofEpochSecond(ts).atZone(ZoneId.of("Asia/Seoul"));

                    if ((time.isEqual(start) || time.isAfter(start)) && time.isBefore(end)) {
                        double change = ((price - prevClose) / prevClose) * 100;
                        change = Math.round(change * 100.0) / 100.0;
                        changes.add(new StockChangePoint(time.toString(), change));
                        System.out.printf("[시간: %s] 변동률: %.2f%%\n", time.toLocalTime(), change);
                    }
                }

                return changes;

            }catch (Exception e){ // 오류 났을때 오류 코드 출력
                e.printStackTrace();
            }
            return Collections.emptyList();
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
