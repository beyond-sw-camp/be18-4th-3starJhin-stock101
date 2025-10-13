package com.monstersinc.stock101.stock.model.service;

import java.util.List;

import com.monstersinc.stock101.stock.model.vo.Stock;

public interface StockService {
    List<Stock> getStockList();

    Stock getStockById(long stockId);
}
