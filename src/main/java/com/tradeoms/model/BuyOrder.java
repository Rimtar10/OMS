package com.tradeoms.model;

import java.math.BigDecimal;

/**
 * A BUY order — the user wants to purchase an instrument.
 * The matching engine will look for a SELL order at the same or lower price.
 */
public class BuyOrder extends Order {

    public BuyOrder(String userId, Instrument instrument, int quantity, BigDecimal price) {
        super(userId, instrument, quantity, price);
    }

    @Override
    public OrderType getType() {
        return OrderType.BUY;
    }
}