package com.monstersinc.stock101.service;

import com.monstersinc.stock101.exception.GlobalException;
import com.monstersinc.stock101.exception.message.GlobalExceptionMessage;
import com.monstersinc.stock101.stock.model.mapper.StockMapper;
import com.monstersinc.stock101.stock.model.service.StockServiceImpl;
import com.monstersinc.stock101.stock.model.vo.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockMapper stockMapper;

    @InjectMocks
    private StockServiceImpl stockService;

    @Test
    void getStockList_정상_리턴() {
        // given
        List<Stock> mockList = List.of(new Stock(), new Stock());
        given(stockMapper.selectStockList()).willReturn(mockList);

        // when
        List<Stock> result = stockService.getStockList();

        // then
        assertThat(result).hasSize(2);
        verify(stockMapper).selectStockList();
    }

    @Test
    void getStockList_빈리스트면_예외발생() {
        // given
        given(stockMapper.selectStockList()).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> stockService.getStockList())
                .isInstanceOf(GlobalException.class)
                .hasMessage(GlobalExceptionMessage.STOCK_NOT_FOUND.getMessage());
    }

    @Test
    void getStockById_정상_리턴() {
        // given
        Stock mockStock = new Stock();
        given(stockMapper.selectStockById(1L)).willReturn(mockStock);

        // when
        Stock result = stockService.getStockById(1L);

        // then
        assertThat(result).isEqualTo(mockStock);
        verify(stockMapper).selectStockById(1L);
    }

    @Test
    void getStockById_null이면_예외발생() {
        // given
        given(stockMapper.selectStockById(1L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> stockService.getStockById(1L))
                .isInstanceOf(GlobalException.class)
                .hasMessage(GlobalExceptionMessage.STOCK_NOT_FOUND.getMessage());
    }
}
