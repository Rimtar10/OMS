package com.tradeoms.repository;

import com.tradeoms.db.DatabaseConnection;
import com.tradeoms.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations for Trades.
 * A trade is created whenever two orders match — it is the permanent record
 * of the transaction.
 */
public class TradeRepository {

    /**
     * Saves a new trade to the database.
     * After saving, the auto-generated ID is set back on the trade object.
     */
    public void save(Trade trade) throws SQLException {
        String sql = "INSERT INTO trades (buy_order_id, sell_order_id, instrument_id, quantity, price) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, trade.getBuyOrder().getId());
            stmt.setLong(2, trade.getSellOrder().getId());
            stmt.setString(3, trade.getInstrument().getId());
            stmt.setInt(4, trade.getQuantity());
            stmt.setBigDecimal(5, trade.getPrice());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                trade.setId(keys.getLong(1));
            }

            System.out.printf("[TRADE EXECUTED] %s | Qty: %d @ %s | Buyer: %s | Seller: %s%n",
                    trade.getInstrument().getSymbol(),
                    trade.getQuantity(),
                    trade.getPrice(),
                    trade.getBuyOrder().getUserId(),
                    trade.getSellOrder().getUserId());
        }
    }

    /**
     * Returns all trades ever executed.
     * Useful for reporting and audit history.
     */
    public List<Trade> findAll(List<Order> orders, List<Instrument> instruments) throws SQLException {
        String sql = "SELECT * FROM trades ORDER BY executed_at DESC";
        List<Trade> trades = new ArrayList<>();

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trades.add(mapRow(rs, orders, instruments));
            }
        }
        return trades;
    }

    private Trade mapRow(ResultSet rs, List<Order> orders,
                         List<Instrument> instruments) throws SQLException {

        long buyOrderId  = rs.getLong("buy_order_id");
        long sellOrderId = rs.getLong("sell_order_id");
        String instrId   = rs.getString("instrument_id");

        Order buyOrder = orders.stream()
                .filter(o -> o.getId() == buyOrderId)
                .findFirst()
                .orElseThrow(() -> new SQLException("Buy order not found: " + buyOrderId));

        Order sellOrder = orders.stream()
                .filter(o -> o.getId() == sellOrderId)
                .findFirst()
                .orElseThrow(() -> new SQLException("Sell order not found: " + sellOrderId));

        Instrument instrument = instruments.stream()
                .filter(i -> i.getId().equals(instrId))
                .findFirst()
                .orElseThrow(() -> new SQLException("Instrument not found: " + instrId));

        Trade trade = new Trade(buyOrder, sellOrder, instrument,
                rs.getInt("quantity"), rs.getBigDecimal("price"));
        trade.setId(rs.getLong("id"));
        return trade;
    }
}