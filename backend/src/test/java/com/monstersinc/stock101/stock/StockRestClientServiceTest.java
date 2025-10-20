package com.monstersinc.stock101.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monstersinc.stock101.restclient.stock.model.dto.GetStockCodeDto;
import com.monstersinc.stock101.restclient.stock.model.mapper.StockRestClientMapper;
import com.monstersinc.stock101.restclient.stock.model.service.StockRestClientServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StockRestClientServiceTest {

    @Mock private StockRestClientMapper mapper;
    @Mock private RestTemplate restTemplate;
    @Mock private ObjectMapper objectMapper;
    @Mock private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private StockRestClientServiceImpl service;

    @Test
    void testGetAllStockCodes_returnsList() {
        // given
        when(mapper.getAllStockCodes()).thenReturn(List.of(new GetStockCodeDto(1L, "AAPL")));

        // when
        List<GetStockCodeDto> result = service.getAllStockCodes();

        // then
        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getStockCode());
        verify(mapper).getAllStockCodes();
    }
}
