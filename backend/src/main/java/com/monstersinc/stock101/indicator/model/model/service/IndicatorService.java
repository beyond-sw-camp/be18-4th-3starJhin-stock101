package com.monstersinc.stock101.indicator.model.model.service;

import com.monstersinc.stock101.indicator.model.model.vo.AnalystIndicator;
import com.monstersinc.stock101.indicator.model.model.vo.FinancialIndicator;
import com.monstersinc.stock101.indicator.model.model.vo.IndividualIndicator;
import com.monstersinc.stock101.indicator.model.model.vo.NewsIndicator;

public interface IndicatorService {
    IndividualIndicator getIndividualIndicator(long stockId);

    AnalystIndicator getAnalystIndicator(long stockId);

    NewsIndicator getNewsIndicator(long stockId);

    FinancialIndicator getFinancialIndicator(long stockId);
}
