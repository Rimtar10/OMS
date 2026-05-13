package com.tradeoms.factory;

import com.tradeoms.model.*;

import java.math.BigDecimal;

public class OrderFactory {

    // Private constructor — this class should never be instantiated, only used statically
    private OrderFactory() {}


    public static Order create(OrderType type, String userId,
                               Instrument instrument, int quantity, BigDecimal price) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }

        return switch (type) {
            case BUY  -> new BuyOrder(userId, instrument, quantity, price);
            case SELL -> new SellOrder(userId, instrument, quantity, price);
        };
    }
}