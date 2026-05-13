package com.tradeoms.engine;

import com.tradeoms.model.*;
import com.tradeoms.observer.OrderObserver;
import com.tradeoms.repository.OrderRepository;
import com.tradeoms.repository.TradeRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderMatchingEngine {

    // One order book per instrument — e.g. separate books for AAPL and TSLA
    private final Map<String, OrderBook> orderBooks = new HashMap<>();

    private MatchingStrategy strategy;
    private final List<OrderObserver> observers = new ArrayList<>();

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    public OrderMatchingEngine(MatchingStrategy strategy,
                               OrderRepository orderRepository,
                               TradeRepository tradeRepository) {
        this.strategy         = strategy;
        this.orderRepository  = orderRepository;
        this.tradeRepository  = tradeRepository;
    }

    /**
     * Registers an observer to receive order status update notifications.
     */
    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    /**
     * Swaps the matching strategy at runtime — Strategy pattern in action.
     */
    public void setStrategy(MatchingStrategy strategy) {
        this.strategy = strategy;
    }

    public void submitOrder(Order order) throws SQLException {
        // Step 1: persist the order first
        orderRepository.save(order);
        System.out.printf("[ORDER SUBMITTED] %s %d %s @ %s (ID: %d)%n",
                order.getType(), order.getQuantity(),
                order.getInstrument().getSymbol(), order.getPrice(), order.getId());

        // Step 2: add to the instrument's order book (create book if first order)
        String instrId = order.getInstrument().getId();
        orderBooks.computeIfAbsent(instrId, OrderBook::new).addOrder(order);

        // Step 3: run matching
        OrderBook book = orderBooks.get(instrId);
        List<Trade> trades = strategy.match(book);

        // Step 4: persist each trade and notify observers
        for (Trade trade : trades) {
            tradeRepository.save(trade);

            // Update both matched orders in the database
            orderRepository.update(trade.getBuyOrder());
            orderRepository.update(trade.getSellOrder());

            // Notify all observers about each affected order
            notifyObservers(trade.getBuyOrder());
            notifyObservers(trade.getSellOrder());
        }

        // If the order wasn't matched at all, still notify observers (status = PENDING)
        if (trades.isEmpty()) {
            notifyObservers(order);
        }
    }

    /**
     * Cancels a PENDING order — removes it from the book and marks it CANCELLED.
     */
    public void cancelOrder(Order order) throws SQLException {
        String instrId = order.getInstrument().getId();
        OrderBook book = orderBooks.get(instrId);

        if (book != null) {
            book.removeOrder(order);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.update(order);
        notifyObservers(order);
        System.out.printf("[ORDER CANCELLED] ID: %d | %s %s%n",
                order.getId(), order.getType(), order.getInstrument().getSymbol());
    }

    /**
     * Prints the current state of the order book for a given instrument.
     */
    public void printOrderBook(String instrumentId) {
        OrderBook book = orderBooks.get(instrumentId);
        if (book != null) {
            book.printBook();
        } else {
            System.out.println("No order book found for: " + instrumentId);
        }
    }

    private void notifyObservers(Order order) {
        for (OrderObserver observer : observers) {
            observer.onOrderUpdated(order);
        }
    }
}