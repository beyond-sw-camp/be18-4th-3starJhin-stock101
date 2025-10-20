package com.monstersinc.stock101.indicator.model;

import com.monstersinc.stock101.exception.GlobalException;
import com.monstersinc.stock101.exception.message.GlobalExceptionMessage;
import com.monstersinc.stock101.indicator.model.model.mapper.IndicatorMapper;
import com.monstersinc.stock101.indicator.model.model.service.IndicatorServiceImpl;
import com.monstersinc.stock101.indicator.model.model.vo.AnalystIndicator;
import com.monstersinc.stock101.indicator.model.model.vo.IndividualIndicator;
import com.monstersinc.stock101.indicator.model.model.vo.NewsIndicator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IndicatorServiceImplTest {

    @Mock
    private IndicatorMapper indicatorMapper;

    @InjectMocks
    private IndicatorServiceImpl indicatorService;

    @Test
    void getIndividualIndicator_정상_리턴() {
        IndividualIndicator mockIndicator = new IndividualIndicator();
        given(indicatorMapper.selectIndividualIndicatorByStockId(1L)).willReturn(mockIndicator);

        IndividualIndicator result = indicatorService.getIndividualIndicator(1L);

        assertThat(result).isEqualTo(mockIndicator);
        verify(indicatorMapper).selectIndividualIndicatorByStockId(1L);
    }

    @Test
    void getIndividualIndicator_null이면_예외발생() {
        given(indicatorMapper.selectIndividualIndicatorByStockId(1L)).willReturn(null);

        assertThatThrownBy(() -> indicatorService.getIndividualIndicator(1L))
                .isInstanceOf(GlobalException.class)
                .hasMessage(GlobalExceptionMessage.INDICATOR_NOT_FOUND.getMessage());
    }

    @Test
    void getAnalystIndicator_정상_리턴() {
        AnalystIndicator mockIndicator = new AnalystIndicator();
        given(indicatorMapper.selectAnalystIndicatorByStockId(2L)).willReturn(mockIndicator);

        AnalystIndicator result = indicatorService.getAnalystIndicator(2L);

        assertThat(result).isEqualTo(mockIndicator);
    }

    @Test
    void getAnalystIndicator_null이면_예외발생() {
        given(indicatorMapper.selectAnalystIndicatorByStockId(2L)).willReturn(null);

        assertThatThrownBy(() -> indicatorService.getAnalystIndicator(2L))
                .isInstanceOf(GlobalException.class)
                .hasMessage(GlobalExceptionMessage.INDICATOR_NOT_FOUND.getMessage());
    }

    @Test
    void getNewsIndicator_정상_리턴() {
        NewsIndicator mockIndicator = new NewsIndicator();
        given(indicatorMapper.selectNewsIndicatorByStockId(3L)).willReturn(mockIndicator);

        NewsIndicator result = indicatorService.getNewsIndicator(3L);

        assertThat(result).isEqualTo(mockIndicator);
    }

    @Test
    void getFinancialIndicator_null이면_예외발생() {
        given(indicatorMapper.selectFinancialIndicatorByStockId(4L)).willReturn(null);

        assertThatThrownBy(() -> indicatorService.getFinancialIndicator(4L))
                .isInstanceOf(GlobalException.class)
                .hasMessage(GlobalExceptionMessage.INDICATOR_NOT_FOUND.getMessage());
    }
}