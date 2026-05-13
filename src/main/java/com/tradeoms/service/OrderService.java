package com.tradeoms.service;

import com.tradeoms.engine.OrderMatchingEngine;
import com.tradeoms.factory.OrderFactory;
import com.tradeoms.model.*;
import com.tradeoms.repository.OrderRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * OrderService is the entry point for all order-related business logic.
 *
 * WHY A SERVICE LAYER?
 * The engine handles matching. The repository handles DB.
 * The service layer sits between the caller (Main/UI) and both of those —
 * it validates input, creates the correct objects, and coordinates the flow.
 *
 * The caller never touches the engine or repository directly.
 * They just call: orderService.placeOrder(...)
 */
public class OrderService {

    private final OrderMatchingEngine engine;
    private final OrderRepository orderRepository;
    private final PortfolioService portfolioService;

    public OrderService(OrderMatchingEngine engine,
                        OrderRepository orderRepository,
                        PortfolioService portfolioService) {
        this.engine           = engine;
        this.orderRepository  = orderRepository;
        this.portfolioService = portfolioService;
    }

    /**
     * Places a new order — the main action a user takes.
     *
     * Flow:
     * 1. OrderFactory creates the correct BuyOrder or SellOrder
     * 2. Engine submits it → saves to DB, runs matching, fires observers
     * 3. If matched, PortfolioService updates the user's position
     *
     * @param type       BUY or SELL
     * @param userId     the user placing the order
     * @param instrument the asset to trade
     * @param quantity   number of units
     * @param price      price per unit
     * @return the created Order with its DB-assigned ID
     */
    public Order placeOrder(OrderType type, String userId,
                            Instrument instrument, int quantity,
                            BigDecimal price) throws SQLException {

        Order order = OrderFactory.create(type, userId, instrument, quantity, price);

        engine.submitOrder(order);

        if (order.getStatus() == OrderStatus.FILLED || order.getStatus() == OrderStatus.PARTIAL) {
            int filledQty = (order.getStatus() == OrderStatus.FILLED)
                    ? quantity
                    : quantity - order.getQuantity();

            portfolioService.updatePosition(userId, instrument, type, filledQty, price);
        }

        return order;
    }

    public void cancelOrder(Order order) throws SQLException {
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PARTIAL) {
            throw new IllegalStateException(
                    "Cannot cancel order with status: " + order.getStatus());
        }
        engine.cancelOrder(order);
    }

    public List<Order> getPendingOrders(List<Instrument> instruments) throws SQLException {
        return orderRepository.findByStatus(OrderStatus.PENDING, instruments);
    }

    public void printOrderBook(String instrumentId) {
        engine.printOrderBook(instrumentId);
    }
}