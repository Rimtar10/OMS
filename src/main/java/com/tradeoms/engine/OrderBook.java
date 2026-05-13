package com.tradeoms.engine;

import com.tradeoms.model.*;

import java.util.PriorityQueue;


public class OrderBook {

    private final String instrumentId;

    // Best buyer (highest price) at the front
    private final PriorityQueue<Order> buyOrders = new PriorityQueue<>(
            (a, b) -> {
                int priceCmp = b.getPrice().compareTo(a.getPrice()); // DESC price
                if (priceCmp != 0) return priceCmp;
                return a.getCreatedAt().compareTo(b.getCreatedAt());  // ASC time
            }
    );

    // Best seller (lowest price) at the front
    private final PriorityQueue<Order> sellOrders = new PriorityQueue<>(
            (a, b) -> {
                int priceCmp = a.getPrice().compareTo(b.getPrice()); // ASC price
                if (priceCmp != 0) return priceCmp;
                return a.getCreatedAt().compareTo(b.getCreatedAt()); // ASC time
            }
    );

    public OrderBook(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    /**
     * Adds an order to the appropriate side of the book.
     */
    public void addOrder(Order order) {
        if (order.getType() == OrderType.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
    }

    /**
     * Removes an order (e.g. when it gets fully filled or cancelled).
     */
    public void removeOrder(Order order) {
        if (order.getType() == OrderType.BUY) {
            buyOrders.remove(order);
        } else {
            sellOrders.remove(order);
        }
    }

    public PriorityQueue<Order> getBuyOrders()  { return buyOrders; }
    public PriorityQueue<Order> getSellOrders() { return sellOrders; }
    public String getInstrumentId()             { return instrumentId; }

    /**
     * Prints the current state of the order book — useful for debugging.
     */
    public void printBook() {
        System.out.println("\n===== ORDER BOOK: " + instrumentId + " =====");
        System.out.println("--- BUY ORDERS (highest price first) ---");
        buyOrders.stream()
                .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                .forEach(o -> System.out.printf("  [BUY]  Qty: %-5d Price: %s  User: %s%n",
                        o.getQuantity(), o.getPrice(), o.getUserId()));

        System.out.println("--- SELL ORDERS (lowest price first) ---");
        sellOrders.stream()
                .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .forEach(o -> System.out.printf("  [SELL] Qty: %-5d Price: %s  User: %s%n",
                        o.getQuantity(), o.getPrice(), o.getUserId()));
        System.out.println("==========================================\n");
    }
}