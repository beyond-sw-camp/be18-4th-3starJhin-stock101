package com.monstersinc.stock101.restclient.stock.model.service;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monstersinc.stock101.restclient.stock.model.Insight;
import com.monstersinc.stock101.restclient.stock.model.NewsPolygonResponse;
import com.monstersinc.stock101.restclient.stock.model.PolygonResponse;
import com.monstersinc.stock101.restclient.stock.model.dto.GetNewsDto;
import com.monstersinc.stock101.restclient.stock.model.dto.GetStockCodeDto;
import com.monstersinc.stock101.restclient.stock.model.dto.KisMinuteResp;
import com.monstersinc.stock101.restclient.stock.model.dto.NewsIndicatorDto;
import com.monstersinc.stock101.restclient.stock.model.dto.StockFinancialInfoResDto;
import com.monstersinc.stock101.restclient.stock.model.dto.StockPriceChartDto;
import com.monstersinc.stock101.restclient.stock.model.dto.StockPriceDto;
import com.monstersinc.stock101.restclient.stock.model.dto.TokenResDto;
import com.monstersinc.stock101.restclient.stock.model.mapper.StockRestClientMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockRestClientServiceImpl implements StockRestClientService {

    //application.yml에서 가져오기
    @Value("${apikey.stock-key}")
    private String stockKey;

    @Value("${apikey.kis-key}")
    private String kiskey;

    @Value("${apikey.kis-secret}")
    private String kisSecret;


    private final ObjectMapper objectMapper;

    private StockFinancialInfoResDto stockInfoResDto;

    private final RestTemplate restTemplate;
    private final StockRestClientMapper stockRestClientMapper;
    private final org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;

    // 한국투자증권 api token 발급
@Override
    public void getOAuthToken() {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("grant_type", "client_credentials");
            requestMap.put("appkey", kiskey);
            requestMap.put("appsecret", kisSecret);
            
            String jsonBody = objectMapper.writeValueAsString(requestMap);
            
            HttpEntity<String> requestMessage = new HttpEntity<>(jsonBody, httpHeaders);
            
            String URL = "https://openapi.koreainvestment.com:9443/oauth2/tokenP";
            HttpEntity<String> response = restTemplate.exchange(URL, HttpMethod.POST, requestMessage, String.class);
            
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            
            TokenResDto.TokenRes tokenRes = objectMapper.readValue(response.getBody(), TokenResDto.TokenRes.class);
            // store token in redis
            try {
                Map<String, Object> tokenMap = objectMapper.readValue(response.getBody(), Map.class);
                Object accessTokenObj = tokenMap.get("access_token");
                Object expiresObj = tokenMap.get("expires_in");
                if (accessTokenObj != null) {
                    String accessToken = accessTokenObj.toString();
                    long ttl = 3500L;
                    if (expiresObj instanceof Number) {
                        ttl = ((Number) expiresObj).longValue();
                    } else if (expiresObj != null) {
                        try { ttl = Long.parseLong(expiresObj.toString()); } catch (Exception ignore) {}
                    }
                    redisTemplate.opsForValue().set("oauth:access_token", accessToken);
                    redisTemplate.opsForValue().set("oauth:token_res", response.getBody());
                    redisTemplate.expire("oauth:access_token", java.time.Duration.ofSeconds(Math.max(60, ttl - 60)));
                    redisTemplate.expire("oauth:token_res", java.time.Duration.ofSeconds(Math.max(60, ttl - 60)));
                    System.out.println("Stored OAuth token in Redis (ttl seconds): " + Math.max(60, ttl - 60));
                }
            } catch (Exception e) {
                System.out.println("Failed to store token in redis: " + e.getMessage());
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            tokenRes.accessToken();
            tokenRes.accessTokenTokenExpired();
            tokenRes.TokenType();
            tokenRes.expiresIn();
            System.out.println("Access Token: " + tokenRes.accessToken());
            System.out.println("Token Expiry: " + tokenRes.accessTokenTokenExpired());
            System.out.println("Token Type: " + tokenRes.TokenType());
            System.out.println("Expires In: " + tokenRes.expiresIn() + " seconds");
            System.out.println("Token Retrieved At: " + LocalDateTime.now().format(formatter));
            System.out.println("Token Valid Until: " + LocalDateTime.now().plusSeconds(tokenRes.expiresIn()).format(formatter) );


        }
        catch (Exception e) {
                e.printStackTrace();    
    }
}


    //뉴스 가져오기
    @Override
    public String getNews(){
        try{
            List<GetStockCodeDto> allStockCodes = getAllStockCodes();
            for(GetStockCodeDto getStockCodeDto : allStockCodes) {
                    String ticker = getStockCodeDto.getStockCode();
            LocalDate date = LocalDate.now().minusDays(2);

            // 주말이면 금요일로 보정
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                date = date.minusDays(1);
            } else if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                date = date.minusDays(2);
            }

            String searchDate = date.toString(); // yyyy-MM-dd

            String url = "https://api.polygon.io/v2/reference/news?ticker=" + ticker + "&published_utc=" + searchDate + "&limit=10&sort=published_utc&order=desc&apikey=" + stockKey;
            String newsBody = restTemplate.getForObject(url, String.class);
            System.out.println("News raw body length for " + ticker + ": " + (newsBody == null ? 0 : newsBody.length()));
            NewsPolygonResponse np = null;
            try {
                if (newsBody != null) {
                    np = objectMapper.readValue(newsBody, NewsPolygonResponse.class);
                }
            } catch (Exception ex) {
                System.out.println("Failed to parse news JSON for ticker: " + ticker + " -> " + ex.getMessage());
                // skip this ticker on parse errors
                Thread.sleep(15000);
                continue;
            }

            if (np == null || np.getResults() == null || np.getResults().isEmpty()) {
                System.out.println("No news response for ticker: " + ticker + " on or after " + searchDate);
                Thread.sleep(15000);
                continue;
            }

            System.out.println("Searching news for ticker: " + ticker + " on or after " + searchDate + " size: " + np.getResults().size());

            //newsPolygonResponse을 가공 후 dto로 변환
            for (NewsPolygonResponse.Result result : np.getResults()) {
                try {
                    List<String> tickers = result.getTickers();
                    if (tickers == null || tickers.isEmpty() || !tickers.contains(ticker)) {
                        System.out.println("No news data for ticker: " + ticker);
                        continue;
                    };
                    // 감정 분석
                    // enum 'NEGATIVE','NEUTRAL','POSITIVE'
                    String insight = result.getInsights() == null ? "NEUTRAL" :
                    result.getInsights().stream()
                    .filter(isght -> isght.getTicker().equals(ticker))
                    .map(isght -> isght.getSentiment().toUpperCase())
                    .findFirst()
                    .orElse("NEUTRAL");

                    GetNewsDto getNewsDto = GetNewsDto.builder()
                            .newsId(result.getId())
                            .image_url(result.getImage_url())
                            .title(result.getTitle())
                            .contentSummary(result.getDescription())
                            .articleUrl(result.getArticleUrl())
                            .publishedAt(Timestamp.from(Instant.parse(result.getPublishedUtc())))
                            .ticker(ticker)
                            .result(insight)
                            .stockId(getStockCodeDto.getStockId())
                            .build();
                    
                    // news_indicatior

                    List<Insight> indicatiorInsights = result.getInsights();

                    for (Insight indicatiorInsight : indicatiorInsights) {
                        GetStockCodeDto newsIndicatorStockIdDto = stockRestClientMapper.getStockIdByCode(indicatiorInsight.getTicker());
                        if(newsIndicatorStockIdDto == null){
                            continue;
                        }
                        NewsIndicatorDto newsIndicatorDto =  NewsIndicatorDto.builder()
                        .insightId(result.getPublishedUtc() + result.getId() + indicatiorInsight.getTicker())
                        .result(indicatiorInsight.getSentiment())
                        .date(Timestamp.from(Instant.parse(result.getPublishedUtc())))
                        .ticker(indicatiorInsight.getTicker())
                        .newsId(result.getId())
                        .stockId(newsIndicatorStockIdDto.getStockId())
                        .build();
                        try{
                        stockRestClientMapper.insertInsight(newsIndicatorDto);
                        System.out.println("뉴스 인디케이터 추가중"+ indicatiorInsight.getTicker());
                    } catch(DuplicateKeyException e){
                        System.out.println("pk가 중복입니다");
                    }
                    }
                            
                    //중복 뉴스 방지
                    if (stockRestClientMapper.existsNews(getNewsDto.getNewsId())) {
                        System.out.println("News already exists for ticker: " + ticker + ", newsId: " + getNewsDto.getNewsId());
                        continue;
                    }

                    stockRestClientMapper.insertNews(getNewsDto);
                    System.out.println("News updated for ticker: " + ticker + ", newsId: " + getNewsDto.getNewsId());

                } catch (Exception ex) {
                    System.out.println("Error processing news result for ticker: " + ticker + " -> " + ex.getMessage());
                    // continue to next result
                }
            }
            Thread.sleep(15000); // 15초 대기
            System.out.println("Get News (ticker: " + ticker + ")");
            }
        }catch (Exception e) {
                throw new RuntimeException(e);
            }
        return "News updated successfully.";
    }
        //주식 가격 가져오기
    @Override
    public List<StockPriceChartDto> getStockprices(String ticker) { 
        try{
            
        String url = "https://openapi.koreainvestment.com:9443/uapi/overseas-price/v1/quotations/inquire-time-itemchartprice";
        Long id = getStockIdByCode(ticker).getStockId();
        System.out.println("Bearer " + getAccessTokenFromRedis());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAccessTokenFromRedis());
        headers.add("appKey", kiskey);
        headers.add("appSecret", kisSecret);
        headers.add("tr_id", "HHDFS76950200"); // 거래ID: 해외주식 상세현재가 조회용

        // 요청 파라미터
        Map<String, String> params = new HashMap<>();
        params.put("AUTH", "");   // 필요시
        params.put("EXCD", "NAS");
        params.put("SYMB", ticker);
        params.put("NMIN", "1");
        params.put("PINC", "1");
        params.put("NEXT", "0");
        params.put("NREC", "60");
        params.put("FILL", "");

        // HttpEntity
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        // 요청 실행
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KisMinuteResp> response = restTemplate.exchange(
                url + "?AUTH=&EXCD=NAS&SYMB="+ticker+"&NMIN=1&PINC=1&NEXT=0&NREC=120&FILL=",
                HttpMethod.GET,
                entity,
                KisMinuteResp.class
        );
        LocalDateTime dt = LocalDateTime.parse(response.getBody().getOutput2().getFirst().getKymd()+response.getBody().getOutput2().getFirst().getKhms() , DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        StockPriceDto stockPriceDto = new StockPriceDto();

        stockPriceDto.setId(id);
        stockPriceDto.setSymbol(ticker);
        stockPriceDto.setClose(Double.parseDouble(response.getBody().getOutput2().getFirst().getLast()));
        String lastCloseRaw = redisTemplate.opsForValue().get("lastprice " + ticker);
        double lastClosePrice = (lastCloseRaw == null || lastCloseRaw.isBlank())
                ? 0.0
                : Double.parseDouble(lastCloseRaw);
        double fluctuation = decimal_round(stockPriceDto.getClose() - (lastClosePrice == 0 ? 0 : lastClosePrice));
        stockPriceDto.setFrom(dt);
        stockPriceDto.setFluctuation(fluctuation);
        stockPriceDto.setPK(ticker+"_"+ dt); // PK 설정
        List<StockPriceChartDto> chartlistChartDtos = new ArrayList<>();
        stockRestClientMapper.updateStockInfo(stockPriceDto); //stocks 테이블 업데이트
        List<KisMinuteResp.Bar> stickBars = response.getBody().getOutput2();
        for(KisMinuteResp.Bar stickBar : stickBars){
            LocalDateTime dtc = LocalDateTime.parse(stickBar.getKymd()+stickBar.getKhms() , DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                StockPriceChartDto stockPriceChartDto = StockPriceChartDto.builder()
                .stockId(id)
                .stockPricePk(ticker+dtc)
                .datetime(dtc)
                .open(Double.parseDouble(stickBar.getOpen()))
                .high(Double.parseDouble(stickBar.getHigh()))
                .low(Double.parseDouble(stickBar.getLow()))
                .last(Double.parseDouble(stickBar.getLast()))
                .build();
                chartlistChartDtos.add(stockPriceChartDto);
                if(LocalTime.of(8, 57,00 ).isAfter(dtc.toLocalTime()) && LocalTime.of(9, 00,00 ).isBefore(dtc.toLocalTime())){
                    redisTemplate.opsForValue().set("lastprice " +  ticker, stickBar.getLast());
                }
        }
        return chartlistChartDtos;
        }catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    //재무제표 가져오기

    // 진짜 머리뽑으면서 짠 코드 
    @Override
    public String getFinancialInfo(String timeframe) {
            try{
                List<GetStockCodeDto> allStockCodes = getAllStockCodes();
                for(GetStockCodeDto getStockCodeDto : allStockCodes) {
                    Long id = getStockCodeDto.getStockId();
                    String ticker = getStockCodeDto.getStockCode();

                
                String url = "https://api.polygon.io/vX/reference/financials"
                    + "?ticker=" + ticker
                    + "&timeframe=" + timeframe 
                    + "&limit=4"
                    + "&apikey=" + stockKey;
                // debug: print the financials URL and response body to help diagnose mapping issues
                System.out.println("Financials URL: " + url);
                String body = restTemplate.getForObject(url, String.class);
                System.out.println("Financials response length: " + (body == null ? 0 : body.length()));
                PolygonResponse pr = objectMapper.readValue(body, PolygonResponse.class);

                if (!(pr.getResults() == null || pr.getResults().size() < 2)) {

                var r0 = pr.getResults().get(0);
                var r1 = pr.getResults().get(1);

                var bs0 = r0.getFinancials().getBalanceSheet();
                var bs1 = r1.getFinancials().getBalanceSheet();
                var is0 = r0.getFinancials().getIncomeStatement();

                double eq0 = nz(bs0.getEquityAttributableToParent().getValue());
                double eq1 = nz(bs1.getEquityAttributableToParent().getValue());
                double assets0 = nz(bs0.getAssets().getValue());
                double assets1 = nz(bs1.getAssets().getValue());

                double netIncome = nz(is0.getNetIncomeLoss().getValue());
                double epsBasic  = nz(is0.getBasicEps().getValue());
                double shares    = nz(is0.getBasicAverageShares().getValue());

                double avgEq = (eq0 + eq1) / 2.0;
                double avgAssets = (assets0 + assets1) / 2.0;

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate ed = LocalDate.parse(r0.getEndDate(), dtf);
                int qt = (ed.getMonthValue()-1)/3 + 1;

                Double roe = (avgEq == 0) ? null : netIncome / avgEq;
                Double roa = (avgAssets == 0) ? null : netIncome / avgAssets;
                Double bps = (shares == 0) ? null : eq0 / shares;
                
                StockFinancialInfoResDto stockInfoResDtoLocal = StockFinancialInfoResDto.builder()
                    .ticker(ticker)
                    .stockId(id)
                    .timeframe(timeframe)
                    .periodEnd(r0.getEndDate())
                    .quarter(qt)
                    .roe(decimal_round(roe))
                    .roa(decimal_round(roa))
                    .eps(decimal_round(Double.isFinite(epsBasic) ? epsBasic : null))
                    .bps(decimal_round(bps))
                    .build();
                if (!stockRestClientMapper.existsFinance(id)) {
                    stockRestClientMapper.insertFinance(stockInfoResDtoLocal);
                } else {
                    stockRestClientMapper.updateFinance(stockInfoResDtoLocal);
                }
                this.stockInfoResDto = stockInfoResDtoLocal;
                System.out.println("Get Stock Finance (ticker: "+ticker+")");
                Thread.sleep(15000); // polygon.io free 정책 분당 5회 제한으로 인한 15초 대기
                }
            }
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
            return "Financial info updated successfully.";
        }
    public List<GetStockCodeDto> getAllStockCodes() {
        return stockRestClientMapper.getAllStockCodes();
    }
    public GetStockCodeDto getStockIdByCode(String ticker){
        return stockRestClientMapper.getStockIdByCode(ticker);
    }

    private double nz(Double val) {
        return (val == null) ? 0.0 : val;
    }
    private Double decimal_round(Double val) {
        if (val == null) return null;
        return Math.round(val * 1000) / 1000.0;
    }
    private final Object oauthLock = new Object();

    private String normalizeTradeDate(String tradeDate) {
        if (tradeDate == null || tradeDate.isBlank()) {
            return LocalDate.now().toString();
        }
        try {
            if (tradeDate.contains("-")) {
                return LocalDate.parse(tradeDate).toString();
            }
            if (tradeDate.length() == 8) {
                return LocalDate.parse(tradeDate, DateTimeFormatter.BASIC_ISO_DATE).toString();
            }
        } catch (Exception ignore) {}
        return LocalDate.now().toString();
    }

    public String getAccessTokenFromRedis() {
    // 1) 먼저 Redis에서 읽기
    String token = redisTemplate.opsForValue().get("oauth:access_token");
    if (token != null && !token.isEmpty()) return token;

    // 2) 없으면 동기화 블록으로 한 번만 발급 요청하도록 함 (싱글인스턴스 환경)
    synchronized (oauthLock) {
        token = redisTemplate.opsForValue().get("oauth:access_token");
        if (token != null && !token.isEmpty()) return token;

        // 호출해서 Redis에 저장되도록 함 (getOAuthToken 내부에서 저장하도록 구현되어 있음)
        getOAuthToken();

        // 발급 후 다시 읽기
        token = redisTemplate.opsForValue().get("oauth:access_token");
        return token;
    }
}
}       
