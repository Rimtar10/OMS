package com.tradeoms.model;

import java.math.BigDecimal;

/**
 * A SELL order — the user wants to sell an instrument they hold.
 * The matching engine will look for a BUY order at the same or higher price.
 */
public class SellOrder extends Order {

    public SellOrder(String userId, Instrument instrument, int quantity, BigDecimal price) {
        super(userId, instrument, quantity, price);
    }

    @Override
    public OrderType getType() {
        return OrderType.SELL;
    }
}