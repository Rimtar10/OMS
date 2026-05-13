package com.tradeoms.model;

import java.math.BigDecimal;


public class SellOrder extends Order {

    public SellOrder(String userId, Instrument instrument, int quantity, BigDecimal price) {
        super(userId, instrument, quantity, price);
    }

    @Override
    public OrderType getType() {
        return OrderType.SELL;
    }
}