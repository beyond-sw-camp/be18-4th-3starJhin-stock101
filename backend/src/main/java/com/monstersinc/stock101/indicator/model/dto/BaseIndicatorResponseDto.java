package com.monstersinc.stock101.indicator.model.dto;

import com.monstersinc.stock101.indicator.model.IndicatorResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseIndicatorResponseDto {
    private IndicatorResult result;
    private LocalDate date;
    private int strongSell;
    private int sell;
    private int hold;
    private int buy;
    private int strongBuy;
    private Long stockId;
}

