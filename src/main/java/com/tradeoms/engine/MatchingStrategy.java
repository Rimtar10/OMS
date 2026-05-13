package com.tradeoms.engine;

import com.tradeoms.model.Trade;
import java.util.List;


public interface MatchingStrategy {

    List<Trade> match(OrderBook orderBook);
}