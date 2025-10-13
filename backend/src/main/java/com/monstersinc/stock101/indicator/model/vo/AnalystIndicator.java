package com.monstersinc.stock101.indicator.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AnalystIndicator {
    private String result;      // 결과 (STRONG_SELL, SELL, HOLD, BUY, STRONG_BUY)
    private Date date; // 날짜
    private int strongSell;     // 적극 판매 카운트
    private int sell;           // 판매 카운트
    private int hold;           // 중립 카운트
    private int buy;            // 구매 카운트
    private int strongBuy;      // 적극 구매 카운트
    private Long stockId;       // 종목 ID
}
