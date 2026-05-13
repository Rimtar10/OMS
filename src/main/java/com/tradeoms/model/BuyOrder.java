package com.tradeoms.model;

import java.math.BigDecimal;


public class BuyOrder extends Order {

    public BuyOrder(String userId, Instrument instrument, int quantity, BigDecimal price) {
        super(userId, instrument, quantity, price);
    }

    @Override
    public OrderType getType() {
        return OrderType.BUY;
    }
}