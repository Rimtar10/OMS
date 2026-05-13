package com.tradeoms.observer;

import com.tradeoms.model.Order;

import java.time.format.DateTimeFormatter;


public class OrderLogger implements OrderObserver {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onOrderUpdated(Order order) {
        String timestamp = order.getCreatedAt().format(FORMATTER);
        System.out.printf("[ORDER UPDATE] %s | ID: %d | Type: %s | Instrument: %s | " +
                        "Qty: %d | Price: %s | Status: %s | User: %s%n",
                timestamp,
                order.getId(),
                order.getType(),
                order.getInstrument().getSymbol(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getUserId()
        );
    }
}